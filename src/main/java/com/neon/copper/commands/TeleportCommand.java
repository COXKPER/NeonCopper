package com.neon.copper.commands;

import com.neon.copper.Permission;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public class TeleportCommand extends Command {

    public TeleportCommand(Permission permissionManager) {
        super("tp");

        var targetArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
        var xArg = ArgumentType.Double("x");
        var yArg = ArgumentType.Double("y");
        var zArg = ArgumentType.Double("z");

        // Teleport to another player
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String uuid = player.getUuid().toString();
            if (!permissionManager.checkUuid(uuid).hasAccess()) {
                player.sendMessage("§cYou don't have permission to use /tp.");
                return;
            }

            var finder = context.get(targetArg);
var target = finder.findFirstPlayer(sender); // Finds the first matching player
if (target == null) {
    player.sendMessage("§cPlayer not found!");
    return;
}
player.teleport(target.getPosition());
            player.sendMessage("§aTeleported to §f" + target.getUsername());
        }, targetArg);

        // Teleport to coordinates
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String uuid = player.getUuid().toString();
            if (!permissionManager.checkUuid(uuid).hasAccess()) {
                player.sendMessage("§cYou don't have permission to change your position.");
                return;
            }

            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);
            player.teleport(new Pos(x, y, z));
            player.sendMessage("§aTeleported to position (" + x + ", " + y + ", " + z + ").");
        }, xArg, yArg, zArg);
    }
}
