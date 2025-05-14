package org.scrapper.utils;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader {

    public static <T> List<T> loadPlugins(String pluginFolderPath, Class<T> pluginType) {
        List<T> loaded = new ArrayList<>();
        File pluginFolder = new File(pluginFolderPath);

        if (!pluginFolder.exists() || !pluginFolder.isDirectory()) return loaded;

        File[] jars = pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) return loaded;

        for (File jar : jars) {
            try {
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{jar.toURI().toURL()},
                        PluginLoader.class.getClassLoader()
                );

                ServiceLoader<T> serviceLoader = ServiceLoader.load(pluginType, classLoader);
                for (T plugin : serviceLoader) {
                    loaded.add(plugin);
                    System.out.println("Plugin chargé : " + plugin.getClass().getName());
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement plugin : " + jar.getName() + " : " + e.getMessage());
            }
        }

        return loaded;
    }
}
