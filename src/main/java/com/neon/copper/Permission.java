package com.neon.copper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Permission {
    private static final String FILE_NAME = "permission.yml";
    private final Map<String, String> permissionMap = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(Permission.class);

    // Load on creation
    public Permission() {
        loadFromFile();
    }

    // Load from permission.yml
    private void loadFromFile() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return;

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileReader(file));
            if (data != null && data.containsKey("permissions")) {
                Map<String, Object> perms = (Map<String, Object>) data.get("permissions");
                for (Map.Entry<String, Object> entry : perms.entrySet()) {
                    permissionMap.put(entry.getKey(), entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load permission.yml: " + e.getMessage());
        }
    }

    // Save to file
    public void saveToFile() {
        try {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("permissions", permissionMap);

            Yaml yaml = new Yaml();
            FileWriter writer = new FileWriter(FILE_NAME);
            yaml.dump(output, writer);
            writer.close();
        } catch (IOException e) {
            logger.error("Failed to save permission.yml: " + e.getMessage());
        }
    }

    // Set permission
    public void setPermission(String uuid, String level) {
        permissionMap.put(uuid, level);
    }

    // Chainable access check
    public Access checkUuid(String uuid) {
        String level = permissionMap.getOrDefault(uuid, "member");
        return new Access(level.equalsIgnoreCase("operator"));
    }

    // Inner access class
    public static class Access {
        private final boolean hasAccess;

        public Access(boolean access) {
            this.hasAccess = access;
        }

        public boolean hasAccess() {
            return hasAccess;
        }
    }
}
