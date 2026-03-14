package com.neon.copper.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class VersionCommand extends Command {

    private static final String SERVER_NAME = "Hestia";
    private static final String SERVER_VERSION = "1.1.0";

    public VersionCommand() {
        super("version", "ver");

        setDefaultExecutor((sender, context) -> {
            String minestomVer = MinecraftServer.VERSION_NAME;
            String javaVer = System.getProperty("java.version");

            String msg = "§6🛈 " + SERVER_NAME + " §fv" + SERVER_VERSION + "\n"
                    + "§7Minecraft/Minestom: §f" + minestomVer + "\n"
                    + "§7Java: §f" + javaVer + "\n"
                    + "§7Repository: §fhttps://github.com/COXKPER/NeonCopper";

            if (sender instanceof Player player) {
                player.sendMessage(msg);
            } else {
                sender.sendMessage(msg);
            }
        });
    }
}
