package com.neon.copper.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.entity.Player;
import com.neon.copper.PluginLoader;

public class PluginListCommand extends Command {

    public PluginListCommand() {
        super("plugins", "pl"); // aliases

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("§6🛈 Plugins (" + PluginLoader.loadedPlugins.size() + "):");

                for (PluginLoader.PluginInfo plugin : PluginLoader.loadedPlugins) {
                    player.sendMessage("§f- §a" + plugin.name + " §7v" + plugin.version + " §8by " + plugin.author);
                }
            } else {
                sender.sendMessage("Plugins (" + PluginLoader.loadedPlugins.size() + "):");
                for (PluginLoader.PluginInfo plugin : PluginLoader.loadedPlugins) {
                    sender.sendMessage("- " + plugin.name + " v" + plugin.version + " by " + plugin.author);
                }
            }
        });
    }
}
