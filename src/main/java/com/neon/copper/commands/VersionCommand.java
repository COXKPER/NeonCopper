package com.neon.copper.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class VersionCommand extends Command {

    private static final String SERVER_NAME = "NeonCopper";
    private static final String SERVER_VERSION = "1.2.0"; // your custom server version

    public VersionCommand() {
        super("version", "ver");

        setDefaultExecutor((sender, context) -> {
            String minestomVer = MinecraftServer.VERSION_NAME;
            String javaVer = System.getProperty("java.version");

            String msg = "§6" + SERVER_NAME + " §fv" + SERVER_VERSION + "\n"
                    + "§7Minestom: §f" + minestomVer + "\n"
                    + "§7Java: §f" + javaVer + "\n"
                    + "The Copyight Owned By Neon Corporation, But We Dont Take Action Of The Cloning Or Change The Codebase" + "\n"
                    + "Github Project, Neon Corporation 2025";

            if (sender instanceof Player player) {
                player.sendMessage(msg);
            } else {
                sender.sendMessage(SERVER_NAME + " v" + SERVER_VERSION);
                sender.sendMessage("Minestom: " + minestomVer);
                sender.sendMessage("Java: " + javaVer);
            }
        });
    }
}
