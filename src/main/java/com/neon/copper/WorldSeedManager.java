package com.neon.copper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Persists a single world seed so terrain generation remains stable across restarts.
 */
public final class WorldSeedManager {
    private static final Logger LOGGER = LogManager.getLogger(WorldSeedManager.class);

    private WorldSeedManager() {
    }

    public static long resolveSeed(Path seedFile, long configuredSeed) {
        try {
            if (Files.exists(seedFile)) {
                String raw = Files.readString(seedFile).trim();
                long loaded = Long.parseLong(raw);
                LOGGER.info("Using persisted world seed: {}", loaded);
                return loaded;
            }

            long seedToSave = configuredSeed;
            if (seedToSave == 0L) {
                seedToSave = ThreadLocalRandom.current().nextLong(Long.MIN_VALUE + 1, Long.MAX_VALUE);
                LOGGER.warn("Configured seed is 0, generated new persistent seed: {}", seedToSave);
            }

            Files.createDirectories(seedFile.getParent());
            Files.writeString(seedFile, Long.toString(seedToSave));
            LOGGER.info("Created world seed file at {}", seedFile);
            return seedToSave;
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("Failed to handle world seed file, falling back to configured seed.", e);
            return configuredSeed;
        }
    }
}
