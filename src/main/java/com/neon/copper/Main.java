package com.neon.copper;

import com.neon.copper.Userdata;
import io.github.togar2.fluids.Fluid;
import io.github.togar2.fluids.FluidState;
import io.github.togar2.fluids.MinestomFluids;
import io.github.togar2.fluids.WaterlogHandler;
import net.minestom.server.extras.lan.OpenToLAN;
import com.neon.copper.commands.StopCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSkinInitEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Point;
import com.neon.copper.CustomNoise;
import com.neon.copper.terminal.CopperTerminalConsole;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.timer.SchedulerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.hollowcube.polar.PolarLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import net.minestom.server.command.builder.Command;
import java.nio.file.StandardOpenOption;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.timer.TaskSchedule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neon.copper.ServerConfig;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Objects;
import net.minestom.server.event.server.ServerListPingEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.io.File;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import java.time.Duration;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.item.Material;
import net.minestom.server.item.ItemStack;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.entity.ItemEntity;
import java.util.Random;
import com.neon.copper.commands.SetPermissionCommand;
import com.neon.copper.commands.GamemodeCommand;
import net.minestom.server.entity.GameMode;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import com.neon.copper.PluginLoader;
import com.neon.copper.commands.PluginListCommand;
import com.neon.copper.commands.VersionCommand;
import com.neon.copper.LogRotator;
import lombok.*;
import com.google.gson.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import java.time.LocalDate;
import java.nio.file.StandardCopyOption;
import java.io.UncheckedIOException;
import java.util.Comparator;




public class Main {
    private final Map<UUID, JsonObject> preloadedData = new ConcurrentHashMap<>();
    private final Path dataDir = Path.of("userdata");
    private static final String SERVER_VERSION = "1.4.1";
     private static final Set<Point> trackedFallingBlocks = new HashSet<>();
     private static final Logger logger = LogManager.getLogger(Main.class);
     private static final Map<UUID, JsonObject> preloadData = new ConcurrentHashMap<>();
     private static String cachedFavicon;

private static void copyFolder(Path source, Path target) throws IOException {
    Files.walk(source).forEach(path -> {
        try {
            Path destination = target.resolve(source.relativize(path));
            if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });
}

private static void restoreFolder(Path backup, Path target) throws IOException {
    deleteRecursively(target);
    copyFolder(backup, target);
    logger.info("Restored world from full folder backup.");
}

private static void deleteRecursively(Path path) throws IOException {
    if (Files.notExists(path)) return;
    Files.walk(path)
         .sorted(Comparator.reverseOrder())
         .forEach(p -> {
             try {
                 Files.deleteIfExists(p);
             } catch (IOException e) {
                 throw new RuntimeException("Failed to delete: " + p, e);
             }
         });
}




    public static void main(String[] args) throws IOException{
        File logDir = new File("logs");
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                logger.info("Created Logging");
            } else {
                System.err.println("Cannot Creating Directory, Forcing Close");
                System.exit(1);
            }
        }
        logger.info("Starting logger rotator.");
        LogRotator.hook();
        logger.info("Starting configuration and permission.");
        Permission permissionManager = new Permission();
        ServerConfig config = new ServerConfig("server.properties");
        String motd         = config.get("motd");
        int    maxPlayers   = Integer.parseInt(config.get("max-players"));
        long   seed         = Long.parseLong(config.get("seed"));        
        int    port         = Integer.parseInt(config.get("port"));
        int    viewDistance = Integer.parseInt(config.get("view-distance"));
        boolean onlineMode  = Boolean.parseBoolean(config.get("online-mode"));


        // Initialization
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    logger.info("Stopping Server");

    MinecraftServer.getInstanceManager().getInstances().forEach(instance -> {
        logger.info("Saving world");
        var polarLoader = new PolarLoader(new PolarWorld());
        ((InstanceContainer) instance).setChunkLoader(polarLoader);
        instance.saveInstance().join();

        try {
            Files.write(Path.of("./world/overworld.mca"), PolarWriter.write(polarLoader.world()), StandardOpenOption.CREATE);
        } catch (Exception e) {
            logger.error("Failed to save world", e);
        }
    });
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error("Error, You Interrupted It");
            e.printStackTrace();
        }


    MinecraftServer.stopCleanly();
}));


        System.setProperty("minestom.chunk-view-distance", String.valueOf(viewDistance));
        System.setProperty("minestom.use-new-chunk-sending", "true");
        System.setProperty("minestom.terminal.disabled", "false");
        System.setProperty("minestom.new-chunk-sending-count-per-interval", "50");
        System.setProperty("minestom.new-chunk-sending-send-interval", "1");
        Path worldFolder = Paths.get("./world");
        Path polarWorldPath = worldFolder.resolve("overworld.mca");
        logger.info("Copper Server By COXPER Corporation");
        logger.warn("This is very light server and we forget implement ai and a gravity, try a ai plugin!");
            LocalDate today = LocalDate.now();
            if (today.getMonthValue() == 5 && today.getDayOfMonth() == 27) {
                // Send secret message to player
                logger.info("27 May Easter Egg");
                logger.info("Kami Dari, 27 Bulan Mei Bulan mei, ayo dong bantai kami, kalo elo punya nyali");
            }
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinestomFluids.init();
        PluginLoader.loadPlugins(new File("plugins"));
        try {
            if (!Files.exists(worldFolder)) {
                Files.createDirectory(worldFolder);
                logger.info("Generating");
            }
        } catch (Exception e) {
            logger.error("Failed to create world folder: " + e.getMessage());
        }
        // CustomNoise noise = new CustomNoise(seed, 0.002, 10, 6, 0.4);
        CustomNoise noise = new CustomNoise(seed, 0.002, 10, 6, 0.5);
        logger.info("Starting Copper Server " + SERVER_VERSION);

        MinecraftServer.getCommandManager().setUnknownCommandCallback((sender, command) -> {
            sender.sendMessage("§cUnknown command.");
        });
        


        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
try {
    if (Files.exists(polarWorldPath)) {
        // Backup the .mca file only if not already backed up
        Path mcaFile = polarWorldPath.resolve("overworld.mca");
        Path mcaBackup = polarWorldPath.resolve("overworld.mca.bak");

        if (Files.exists(mcaFile) && !Files.exists(mcaBackup)) {
            Files.copy(mcaFile, mcaBackup, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Created MCA backup at " + mcaBackup);
        }

        // Backup full folder to folder.bak if not already backed up
        Path backupPath = Paths.get(polarWorldPath + ".bak");
        if (!Files.exists(backupPath)) {
            copyFolder(polarWorldPath, backupPath);
            logger.info("Backed up world folder to " + backupPath);
        } else {
            logger.warn("World folder backup already exists: " + backupPath);
        }

        try {
            // First attempt to load world
            instanceContainer.setChunkLoader(new PolarLoader(polarWorldPath));
            logger.info("World loaded successfully.");

        } catch (Exception loadException) {
            logger.warn("Failed to load world, trying MCA recovery...", loadException);

            // Try MCA restore
            if (Files.exists(mcaBackup)) {
                try {
                    Files.deleteIfExists(mcaFile);
                    Files.copy(mcaBackup, mcaFile, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Restored 'overworld.mca' from backup.");

                    // Retry loading
                    instanceContainer.setChunkLoader(new PolarLoader(polarWorldPath));
                    logger.info("Successfully loaded world after MCA recovery.");
                    return; // successful, stop here

                } catch (IOException retryEx) {
                    logger.error("MCA recovery failed", retryEx);
                }
            } else {
                logger.error("No 'overworld.mca.bak' found.");
            }

            // Try full folder restore
            logger.warn("Trying full folder restore from backup...");
            try {
                restoreFolder(backupPath, polarWorldPath);
                instanceContainer.setChunkLoader(new PolarLoader(polarWorldPath));
                logger.info("Successfully loaded world after full folder restore.");
            } catch (Exception restoreEx) {
                logger.error("Full restore failed, deleting world folder...", restoreEx);

                // All recovery failed, delete world
                deleteRecursively(polarWorldPath);
                logger.warn("Deleted corrupted world folder. A fresh world will be created.");
            }
        }

    } else {
        logger.info("World folder not found. Creating new world...");
    }

    // If we reach here, it's either a new world or after deletion
    instanceContainer.setChunkLoader(new PolarLoader(polarWorldPath));
    logger.info("Created or reset world successfully.");

} catch (Exception e) {
    logger.error("World setup failed", e);
}





        // Set the ChunkGenerator
instanceContainer.setGenerator(unit -> {
    Point start = unit.absoluteStart();
    for (int x = 0; x < unit.size().x(); x++) {
        for (int z = 0; z < unit.size().z(); z++) {
            Point columnStart = start.add(x, 0, z);

            // Adjust surface height to be higher than bedrock range
            int surfaceHeight = (int) noise.evaluateNoise(columnStart.x(), columnStart.z()) + 64;
            // zDebuging
            // logger.info("Debug " + noise.evaluateNoise(columnStart.x(), columnStart.z()) + 64); // We Disable It Beacuse It Dev Only, Dont Edit This Or Your Commits Will Not Accepted

            // Bedrock layer from -36 to -30
            for (int y = -64; y <= -43; y++) {
                unit.modifier().setBlock(columnStart.withY(y), Block.BEDROCK);
            }

            // Stone layer from -29 to surfaceHeight - 4
            for (int y = -43; y < surfaceHeight - 3; y++) {
                unit.modifier().setBlock(columnStart.withY(y), Block.STONE);
            }

            // Dirt layer (2 layers)
            for (int y = surfaceHeight - 3; y < surfaceHeight - 1; y++) {
                unit.modifier().setBlock(columnStart.withY(y), Block.DIRT);
            }

            // Top layer - Grass block
            unit.modifier().setBlock(columnStart.withY(surfaceHeight - 1), Block.GRASS_BLOCK);
        }
    }
});



        MinecraftServer.getGlobalEventHandler().addListener(PlayerCommandEvent.class, e -> {
            if ( e.isCancelled() ) return;
            logger.info("{} issued command: {}", e.getPlayer().getUsername(), e.getCommand());
        }).setPriority(Integer.MAX_VALUE);
        instanceContainer.setChunkSupplier(LightingChunk::new);

        // default commands
        MinecraftServer.getCommandManager().register(new StopCommand());
        MinecraftServer.getCommandManager().register(new SetPermissionCommand(permissionManager));
        MinecraftServer.getCommandManager().register(new GamemodeCommand(permissionManager));
        MinecraftServer.getCommandManager().register(new PluginListCommand());
        MinecraftServer.getCommandManager().register(new VersionCommand());



        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
 globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 100, 0));
       });


        globalEventHandler.addChild(MinestomFluids.events());

        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            var player = event.getPlayer();
            logger.info("{} Disconnected From Server ", player.getUsername());
            Userdata.save(event.getPlayer());
            String message = "§c" + player.getUsername() + " left the game";

            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
                p.sendMessage(Component.text(message));
            });
        });
        globalEventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            if(maxPlayers > -1 &&
                    onlinePlayers > maxPlayers) {
               event.getConnection().kick(Component.text("§cThe server is so popular"));
            }
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            Userdata.load(player, instanceContainer);
            String message = "§a" + player.getUsername() + " joined the game";
    
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
                p.sendMessage(Component.text(message));
            });
        });

        globalEventHandler.addListener(PlayerChatEvent.class, event -> {
            String username = event.getPlayer().getUsername();
            String message = event.getRawMessage();

            logger.info("[ " + username + " ]: " + message);
        });



        globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            var player = event.getPlayer();
            logger.info("{} Connected To The Server", player.getUsername());
        });
        try {
            BufferedImage image = ImageIO.read(new File("./server-icon.png")); // Use vanilla file name
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            cachedFavicon = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            outputStream.close();
        } catch (IOException e) {
            cachedFavicon = "";
            logger.error("Server Icon Not Found, Using Default");
        }

        globalEventHandler.addListener(ServerListPingEvent.class, event -> {
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            ResponseData responseData = event.getResponseData();
            responseData.setOnline(onlinePlayers);
            responseData.setMaxPlayer(maxPlayers);
            responseData.setDescription(LegacyComponentSerializer.legacyAmpersand().deserialize(motd));
            if (cachedFavicon != null) {
               event.getResponseData().setFavicon(cachedFavicon);
            }
        });

        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();
        
            //  If Creative, skip dropping the item
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
        
            //  Not creative, drop item
            var material = event.getBlock().registry().material();
            if (material != null) {
                var itemStack = ItemStack.of(material);
                ItemEntity itemEntity = new ItemEntity(itemStack);
                itemEntity.setInstance(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
            }
        });


        EventNode<Event> node = EventNode.all("all"); //accepts all events
        node.addListener(PickupItemEvent.class, event -> {
            var itemStack = event.getItemStack(); //get the itemstack that was picked up
            //make sure the livingentiy is a player
            if (event.getLivingEntity() instanceof Player player) {
                //add the item to the player's inventory
                player.getInventory().addItemStack(itemStack);
            }
        });

                //Accepts only player events
        EventNode<PlayerEvent> playerNode = EventNode.type("players", EventFilter.PLAYER);
        playerNode.addListener(ItemDropEvent.class, event -> {
            ItemEntity itemEntity = new ItemEntity(event.getItemStack());
            itemEntity.setInstance(event.getPlayer().getInstance(), event.getPlayer().getPosition());
            itemEntity.setVelocity(event.getPlayer().getPosition().add(0, 1, 0).direction().mul(16));
            itemEntity.setPickupDelay(Duration.ofMillis(500));
        });
        node.addChild(playerNode);
        globalEventHandler.addChild(node);



         SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
        Set<Material> fallingBlocks = Set.of(Material.SAND, Material.GRAVEL, Material.RED_SAND);

        scheduler.buildTask(() -> {
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                for (Point pos : trackedFallingBlocks) {
                    Material block = instance.getBlock(pos).registry().material();
                    if (fallingBlocks.contains(block) && instance.getBlock(pos.withY(pos.y() - 1)).isAir()) {
                        instance.setBlock(pos, Block.AIR);
                        // Here you would spawn a custom falling entity, if implemented.
                    }
                }
            }
        }).repeat(TaskSchedule.millis(50)).schedule();

       
        if (onlineMode) {
            MojangAuth.init();
        } else {
            logger.error("The Online Mode Is Disabled, You Will Get A Risk");
            logger.error("Install Login Plugin For More Security and 2FA");
        }
        
        logger.info("Minecraft Server Started In 0.0.0.0:" + port);
        // Start the server on that port same as server properties
        minecraftServer.setBrandName("Copper");
        minecraftServer.start("0.0.0.0", port);
        OpenToLAN.open();
        // Load Copper Terminal
        new CopperTerminalConsole().start();
    }

     public static void save(Player player) {
        if (player == null) return;

        player.sendMessage("Saving...");
        var polarLoader = new PolarLoader(new PolarWorld());
        ((InstanceContainer) player.getInstance()).setChunkLoader(polarLoader);
        player.getInstance().saveInstance().join();

        try {
            Files.write(Path.of("./world/overworld.mca"), PolarWriter.write(polarLoader.world()), StandardOpenOption.CREATE);
            logger.info("Saving World");
        } catch (Exception e) {
            logger.error("Failed to save world", e);
        }

        player.sendMessage("Saved!");
    }

    public static void initAutoSave(Player player) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            logger.info("Auto-saving world...");
            save(player);
        }).repeat(TaskSchedule.seconds(60)).schedule();
    }
}
