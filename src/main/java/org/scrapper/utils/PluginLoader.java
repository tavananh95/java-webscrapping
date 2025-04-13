package org.scrapper.utils;


import org.scrapper.plugins.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader {

    public static List<Plugin> loadPlugins(String pluginFolderPath) {
        List<Plugin> plugins = new ArrayList<>();

        File pluginDir = new File(pluginFolderPath);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            System.err.println("Le dossier de plugins est introuvable : " + pluginFolderPath);
            return plugins;
        }

        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) return plugins;

        for (File jar : jarFiles) {
            try {
                URL jarUrl = jar.toURI().toURL();
                URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, Plugin.class.getClassLoader());

                ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, loader);
                for (Plugin plugin : serviceLoader) {
                    plugins.add(plugin);
                    System.out.println("Plugin chargé : " + plugin.getName());
                }

            } catch (Exception e) {
                System.err.println("Échec du chargement du plugin " + jar.getName());
                e.printStackTrace();
            }
        }

        return plugins;
    }
}
