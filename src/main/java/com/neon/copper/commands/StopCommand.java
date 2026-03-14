package com.neon.copper.commands;

import com.neon.copper.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StopCommand extends Command {
    private static final Logger LOGGER = LogManager.getLogger(StopCommand.class);

    public StopCommand() {
        super("stop");

        setCondition((sender, commandString) ->
                sender instanceof ConsoleSender ||
                        (sender instanceof Player p && p.getPermissionLevel() == 4)
        );

        setDefaultExecutor((sender, context) -> stop());
    }

    private void stop() {
        LOGGER.info("Stopping server and saving worlds...");
        Main.saveAllWorlds();
        MinecraftServer.stopCleanly();
    }
}
