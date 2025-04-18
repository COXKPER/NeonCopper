package com.neon.copper.commands;

import com.neon.copper.Permission;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {

    public GamemodeCommand(Permission permissionManager) {
        super("gamemode");

        ArgumentEnum<GameMode> modeArg = ArgumentType.Enum("mode", GameMode.class);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("§cUsage: /gamemode <creative|survival|spectator|adventure>");
        });

        addSyntax((sender, context) -> {
            if (sender instanceof ConsoleSender) {
                sender.sendMessage("§cConsole can't change gamemode.");
                return;
            }

            if (sender instanceof Player player) {
                String uuid = player.getUuid().toString();

                if (!permissionManager.checkUuid(uuid).hasAccess()) {
                    player.sendMessage("§cYou don't have permission to change your gamemode.");
                    return;
                }

                GameMode mode = context.get(modeArg);
                player.setGameMode(mode);
                player.sendMessage("§aGamemode set to " + mode.name());
            }
        }, modeArg);
    }
}
