package com.neon.copper.commands;

import com.neon.copper.Main;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class SaveCommand extends Command {
    public SaveCommand() {
        super("save-all", "save");

        setCondition((sender, commandString) ->
                sender instanceof ConsoleSender ||
                (sender instanceof Player player && player.getPermissionLevel() == 4)
        );

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("§eSaving all worlds...");
            Main.saveAllWorlds();
            sender.sendMessage("§aSave completed.");
        });
    }
}
