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



    public static void loadPlugins(File pluginDir) {
        if (!pluginDir.exists()) {
            if (pluginDir.mkdirs()) {
            } else {
                logger.error("Failed to create plugins folder.");
                return;
            }
        }

        File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jars == null || jars.length == 0) {
            logger.warn("No plugins found.");
            return;
        }

        for (File jar : jars) {
            try (JarFile jarFile = new JarFile(jar)) {

                JarEntry entry = jarFile.getJarEntry("plugin.yml");
                if (entry == null) {
                    logger.error("plugin.yml missing in " + jar.getName());
                    continue;
                }

                InputStream is = jarFile.getInputStream(entry);
                Properties props = new Properties();
                props.load(is);

                String mainClassName = props.getProperty("main");
                if (mainClassName == null) {
                    logger.error("'main' class not defined in plugin.yml of " + jar.getName());
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
                    logger.info("Loaded plugin: " + props.getProperty("name") + " v" + props.getProperty("version"));
                                // Inside loadPlugins() after plugin loads successfully:
                    loadedPlugins.add(new PluginInfo(
                        props.getProperty("name", "Unknown"),
                        props.getProperty("version", "N/A"),
                        props.getProperty("author", "Unknown")
                    ));
                } catch (NoSuchMethodException e) {
                    logger.error("Plugin loaded, but no onEnable() method found in: " + mainClassName);
                }

            } catch (Exception e) {
                logger.error("Failed to load plugin: " + jar.getName());
                logger.error(e);
            }
        }
    }
}
