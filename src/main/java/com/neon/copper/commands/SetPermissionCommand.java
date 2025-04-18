package com.neon.copper.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import com.neon.copper.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SetPermissionCommand extends Command {
    private static final Logger LOGGER = LogManager.getLogger(SetPermissionCommand.class);

    public SetPermissionCommand(Permission permissionManager) {
        super("setperm");

        ArgumentString uuidArg = ArgumentType.String("uuid");
        ArgumentWord levelArg = ArgumentType.Word("level")
            .from("operator", "member");

        // Command help
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /setperm <uuid> <operator|member>");
        });

        // Command logic
        addSyntax((sender, context) -> {

            // ✅ Console has access
            if (!(sender instanceof ConsoleSender)) {
                if (sender instanceof Player player) {
                    String uuid = player.getUuid().toString();
                    if (!permissionManager.checkUuid(uuid).hasAccess()) {
                        sender.sendMessage("§cYou don't have permission to use this command.");
                        return;
                    }
                }
            }

            // ✅ Now safe to continue
            String uuid = context.get(uuidArg);
            String level = context.get(levelArg);

            permissionManager.setPermission(uuid, level);
            permissionManager.saveToFile();

            if (sender instanceof ConsoleSender) {
              LOGGER.info("Set permission of " + uuid + " to " + level);
            } else {
              sender.sendMessage("§aSet permission of " + uuid + " to " + level);
            }
        }, uuidArg, levelArg);
    }
}
