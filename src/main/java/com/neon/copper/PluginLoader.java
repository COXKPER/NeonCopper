package com.neon.copper;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {
    private static final Logger logger = LogManager.getLogger(PluginLoader.class);
    public static final int SUPPORTED_API_LEVEL = 4;
    public static final List<PluginInfo> loadedPlugins = new ArrayList<>();

    public static class PluginInfo {
        public final String name;
        public final String version;
        public final String author;

        public PluginInfo(String name, String version, String author) {
            this.name = name;
            this.version = version;
            this.author = author;
        }
    }

    public static void redirectSystemStreams() {
        System.setOut(new PrintStream(new LoggingOutputStream(logger, false), true));
        System.setErr(new PrintStream(new LoggingOutputStream(logger, true), true));
    }

    public static void loadPlugins(File pluginDir) {
        logger.info("Starting plugins.");
        if (!pluginDir.exists() && !pluginDir.mkdirs()) {
            logger.error("Failed to create plugins folder.");
            return;
        }

        File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logger.warn("No plugins found.");
            return;
        }

        for (File jar : jars) {
            try (JarFile jarFile = new JarFile(jar)) {

                JarEntry entry = jarFile.getJarEntry("neoplugin.yml");
                if (entry == null) {
                    logger.error("neoplugin.yml missing in " + jar.getName());
                    continue;
                }

                InputStream is = jarFile.getInputStream(entry);
                Properties props = new Properties();
                props.load(is);

                String mainClassName = props.getProperty("main");
                String pluginName = props.getProperty("name", "Unknown");
                String pluginVersion = props.getProperty("version", "N/A");
                String pluginAuthor = props.getProperty("author", "Unknown");
                int apiLevel = Integer.parseInt(props.getProperty("api", "0"));

                if (apiLevel != SUPPORTED_API_LEVEL) {
                    logger.error("Incompatible API level in plugin " + pluginName + ": " + apiLevel + " (required: " + SUPPORTED_API_LEVEL + ")");
                    continue;
                }

                URLClassLoader loader = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    PluginLoader.class.getClassLoader()
                );

                Class<?> pluginClass = loader.loadClass(mainClassName);
                Object pluginInstance = pluginClass.getDeclaredConstructor().newInstance();

                try {
                    Method onEnable = pluginClass.getMethod("onEnable");
                    onEnable.invoke(pluginInstance);
                    logger.info("Loaded plugin: " + pluginName + " v" + pluginVersion);
                    loadedPlugins.add(new PluginInfo(pluginName, pluginVersion, pluginAuthor));
                } catch (NoSuchMethodException e) {
                    logger.error("Plugin loaded but no onEnable() found in: " + mainClassName);
                }

            } catch (Exception e) {
                logger.error("Failed to load plugin: " + jar.getName());
                logger.error(e);
            }
        }
    }

    // Custom OutputStream to redirect println to logger
    private static class LoggingOutputStream extends OutputStream {
        private final Logger logger;
        private final boolean isError;
        private final StringBuilder buffer = new StringBuilder();

        public LoggingOutputStream(Logger logger, boolean isError) {
            this.logger = logger;
            this.isError = isError;
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                flushBuffer();
            } else {
                buffer.append((char) b);
            }
        }

        private void flushBuffer() {
            if (buffer.length() > 0) {
                String msg = buffer.toString().trim();
                if (!msg.isEmpty()) {
                    if (isError) {
                        logger.error("[Plugin] " + msg);
                    } else {
                        logger.info("[Plugin] " + msg);
                    }
                }
                buffer.setLength(0);
            }
        }

        @Override
        public void flush() {
            flushBuffer();
        }
    }
}
