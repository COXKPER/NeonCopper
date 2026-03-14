package com.neon.copper.commands;

import net.minestom.server.command.builder.Command;

public class SeedCommand extends Command {
    public SeedCommand(long worldSeed) {
        super("seed");

        setDefaultExecutor((sender, context) ->
                sender.sendMessage("§6World seed: §f" + worldSeed)
        );
    }
}
