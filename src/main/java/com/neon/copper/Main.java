package com.neon.copper;

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

public class Main {
    private static final String SERVER_VERSION = "1.2.0";
     private static final Set<Point> trackedFallingBlocks = new HashSet<>();
     private static final Logger logger = LogManager.getLogger(Main.class);
     private static String cachedFavicon;

    public static void main(String[] args) throws IOException{
        Permission permissionManager = new Permission();
        ServerConfig config = new ServerConfig("server.properties");
        String motd         = config.get("motd");
        int    maxPlayers   = Integer.parseInt(config.get("max-players"));
        long   seed         = Long.parseLong(config.get("seed"));        // primitive long
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
            logger.info("World Saved");
        } catch (Exception e) {
            logger.error("Failed to save world", e);
        }
    });

    MinecraftServer.stopCleanly();
}));


        System.setProperty("minestom.chunk-view-distance", String.valueOf(viewDistance));
        System.setProperty("minestom.use-new-chunk-sending", "true");

        System.setProperty("minestom.new-chunk-sending-count-per-interval", "50");
        System.setProperty("minestom.new-chunk-sending-send-interval", "1");
        Path worldFolder = Paths.get("./world");
        Path polarWorldPath = worldFolder.resolve("overworld.mca");
        logger.info("Neon Copper Server https://copper.nzst.xyz MC 1.21.4");
        logger.info("Made By Neon Devs");
        logger.info("The Player coordinate not Saved, I Dont Really Develop This, But if you Disconnect or stop the server you will Back to that spawn pos again");
        Random rand = new Random();
        int number = rand.nextInt(10000) + 1; // menghasilkan angka 1 sampai 10000

        if (number == 10) {
            logger.info("Buat Orang Indo: Arapa Putra Kontol");
            logger.info("Selamat Atas Pelantikan Pak Prabowo");
        }
        MinecraftServer minecraftServer = MinecraftServer.init();
        PluginLoader.loadPlugins(new File("plugins"));
        try {
            if (!Files.exists(worldFolder)) {
                Files.createDirectory(worldFolder);
                logger.info("World Not Exist, Making New World");
            }
        } catch (Exception e) {
            logger.error("Failed to create world folder: " + e.getMessage());
        }
        CustomNoise noise = new CustomNoise(seed, 0.01, 1.0, 4, 0.5);
        logger.info("Starting Neon Copper Server " + SERVER_VERSION);

        MinecraftServer.getCommandManager().setUnknownCommandCallback((sender, command) -> {
            sender.sendMessage("Unknown command.");
        });
        System.setProperty("minestom.terminal.disabled", "false");


        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        try {
                    if (Files.exists(polarWorldPath)) {
            instanceContainer.setChunkLoader(new PolarLoader(polarWorldPath));
            logger.info("Loading World");
        } else {
            logger.info("Setting Up Chunk Generation");
        }

        } catch (IOException e) {
          e.printStackTrace();
        }

        // Set the ChunkGenerator
instanceContainer.setGenerator(unit -> {
    Point start = unit.absoluteStart();
    for (int x = 0; x < unit.size().x(); x++) {
        for (int z = 0; z < unit.size().z(); z++) {
            Point columnStart = start.add(x, 0, z);

            // Adjust surface height to be higher than bedrock range
            int surfaceHeight = (int) noise.evaluateNoise(columnStart.x(), columnStart.z()) + 64;

            // Bedrock layer from -36 to -30
            for (int y = -36; y <= -30; y++) {
                unit.modifier().setBlock(columnStart.withY(y), Block.BEDROCK);
            }

            // Stone layer from -29 to surfaceHeight - 4
            for (int y = -29; y < surfaceHeight - 3; y++) {
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

        // default commands
        MinecraftServer.getCommandManager().register(new StopCommand());

        var saveCommand = new Command("save");
        saveCommand.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            player.sendMessage("Saving...");
            var polarLoader = new PolarLoader(new PolarWorld());
            ((InstanceContainer) player.getInstance()).setChunkLoader(polarLoader);
            player.getInstance().saveInstance().join();
            try {
                Files.write(Path.of("./world/overworld.mca"), PolarWriter.write(polarLoader.world()), StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.sendMessage("Saved!");
        });
        MinecraftServer.getCommandManager().register(saveCommand);
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
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            var player = event.getPlayer();
            logger.info("User {} Disconnected From Server ", player.getUsername());
        });
        globalEventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            if(maxPlayers > -1 &&
                    onlinePlayers > maxPlayers) {
               logger.info("The Player Trying Join The Full Server But We disconnect it");
               event.getConnection().kick(Component.text("The server is full"));
            }
        });
        globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            var player = event.getPlayer();
            logger.info("User {} Connected To The Server", player.getUsername());
        });
        try {
            BufferedImage image = ImageIO.read(new File("./server-icon.png")); // Use vanilla file name
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            cachedFavicon = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            outputStream.close();
        } catch (IOException e) {
            cachedFavicon = "";
            logger.error("Server Icon Not Found, Ignoring");
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
        
            // ❌ If Creative, skip dropping the item
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
        
            // ✅ Not creative, drop item
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

        logger.info("Minecraft Server Started In 0.0.0.0:" + port);
        if (onlineMode) {
            MojangAuth.init();
        } else {
            logger.warn("The Online Mode Is Disabled, You Will Get A Risk");
            logger.warn("Install Login Plugin For More Security and 2FA");
        }
        
        
        // Start the server on that port same as server properties
        minecraftServer.setBrandName("Neon Copper");
        minecraftServer.start("0.0.0.0", port);
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
