package com.neon.copper;

import java.io.*;
import java.util.Properties;

public class ServerConfig {
    private final Properties properties = new Properties();

    public ServerConfig(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            }
        } else {
            // Default values
            properties.setProperty("motd", "A Copper Minecraft Server");
            properties.setProperty("max-players", "100");
            properties.setProperty("port", "25565");
            properties.setProperty("seed", "1036");
            properties.setProperty("view-distance", "16");
            properties.setProperty("online-mode", "true");
            save(filename);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void save(String filename) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filename)) {
            properties.store(out, "Server Settings");
        }
    }
}
