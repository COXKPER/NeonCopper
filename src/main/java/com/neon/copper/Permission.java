package com.neon.copper;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Permission {
    private static final Logger logger = LogManager.getLogger(Permission.class);
    private static final String FILE_NAME = "permission.json";
    private static final Set<String> VALID_LEVELS = Set.of("member", "operator");

    private final Map<String, String> permissionMap = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Permission() {
        loadFromFile();
    }

    /** Load permissions from JSON file */
    private synchronized void loadFromFile() {
        Path path = Paths.get(FILE_NAME);
        if (!Files.exists(path)) {
            logger.warn("Permission file not found: " + FILE_NAME);
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Type mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> rawMap = gson.fromJson(reader, mapType);

            permissionMap.clear();
            for (var entry : rawMap.entrySet()) {
                String level = entry.getValue().toLowerCase();
                if (VALID_LEVELS.contains(level)) {
                    permissionMap.put(entry.getKey(), level);
                } else {
                    logger.warn("Invalid permission level for " + entry.getKey() + ": " + entry.getValue());
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.error("Failed to load " + FILE_NAME + ": " + e.getMessage(), e);
        }
    }

    /** Save permissions to JSON file */
    public synchronized void saveToFile() {
        Path path = Paths.get(FILE_NAME);
        try {
            // Backup first
            if (Files.exists(path)) {
                Files.copy(path, Paths.get(FILE_NAME + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }

            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(new TreeMap<>(permissionMap), writer);
            }
        } catch (IOException e) {
            logger.error("Failed to save " + FILE_NAME + ": " + e.getMessage(), e);
        }
    }

    /** Set permission with optional save */
    public void setPermission(String uuid, String level, boolean saveNow) {
        String lower = level.toLowerCase();
        if (!VALID_LEVELS.contains(lower)) {
            logger.warn("Rejected invalid permission level: " + level);
            return;
        }

        permissionMap.put(uuid, lower);
        if (saveNow) saveToFile();
    }

    public void setPermission(String uuid, String level) {
        setPermission(uuid, level, false);
    }

    /** Check permission access */
    public Access checkUuid(String uuid) {
        String level = permissionMap.getOrDefault(uuid, "member");
        return new Access(level.equals("operator"));
    }

    /** Access wrapper class */
    public static class Access {
        private final boolean hasAccess;

        public Access(boolean hasAccess) {
            this.hasAccess = hasAccess;
        }

        public boolean hasAccess() {
            return hasAccess;
        }
    }
}
