package com.neon.copper.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class StopCommand extends Command {
    private static final Logger logger = LogManager.getLogger(StopCommand.class);

    public StopCommand() {
        super("stop");

        setCondition((sender, commandString) ->
            sender instanceof ConsoleSender ||
            (sender instanceof Player p && p.getPermissionLevel() == 4)
        );

        setDefaultExecutor((sender, context) -> stop());
    }

    private void stop() {
            logger.info("Stopping Server");
            System.exit(0);  
    }
}
