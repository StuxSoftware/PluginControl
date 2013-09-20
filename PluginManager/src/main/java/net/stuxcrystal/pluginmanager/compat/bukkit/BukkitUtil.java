package net.stuxcrystal.pluginmanager.compat.bukkit;

import net.stuxcrystal.pluginmanager.utils.ReflectionUtil;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for bukkits internal parts.
 */
public final class BukkitUtil {

    /**
     * Returns the class loader of a plugin.
     *
     * @param plugin The plugin to get the class loader.
     * @return The classloader of the plugin.
     */
    public static PluginClassLoader getPluginClassLoader(JavaPlugin plugin, PluginLoader loader) {

        PluginClassLoader pcl = null;

        Map<String, PluginClassLoader> loaders = ReflectionUtil.getFieldQuiet(
                JavaPluginLoader.class, loader, "loaders0"
        );

        // Some implementations fail on using this method.
        // So if there is no such map fall back to the simple variant.
        if (loaders == null)
            return getPluginClassLoader0(plugin);

        pcl = loaders.get(plugin.getName());

        // Fallback Resolver when the first method fails.
        if (pcl == null) {
            pcl = getPluginClassLoader0(plugin);
        }

        return pcl;
    }

    /**
     * Returns the class loader of a plugin while unloading it.
     *
     * @param plugin The plugin to get the class-loader.
     * @return A class-loader object.
     */
    private static PluginClassLoader getPluginClassLoader0(JavaPlugin plugin) {
        ClassLoader cl = plugin.getClass().getClassLoader();
        if (!(cl instanceof PluginClassLoader))
            throw new IllegalArgumentException("This plugin is not designed to disable non Java-Plugins.");
        return (PluginClassLoader) cl;
    }

    /**
     * Lists all files in the directory.
     *
     * @param directory The directory which jar files are to be returned.
     * @param loader    The plugin loader providing the pattern.
     * @return A list of jar files.
     */
    public static List<File> getJarFiles(File directory, PluginLoader loader) {
        List<File> jarFiles = new ArrayList<File>();
        File[] files = directory.listFiles();

        for (File file : files) {
            if (isValidFile(file, loader)) {
                jarFiles.add(file);
            }
        }

        return jarFiles;
    }

    /**
     * Checks if the given file is a valid plugin.
     * @param file   The file to check.
     * @param loader The loader that contains the patterns.
     * @return True if the file is a valid plugin, false otherwise.
     */
    public static boolean isValidFile(File file, PluginLoader loader) {
        return isValidFile(file.getName(), loader);

    }

    /**
     * Checks if the given file is a valid plugin.
     * @param filename The file to check.
     * @param loader   The loader that contains the patterns.
     * @return True if the file is a valid plugin, false otherwise.
     */
    public static boolean isValidFile(String filename, PluginLoader loader) {
        for (Pattern pattern : loader.getPluginFileFilters()) {
            if (pattern.matcher(filename).find()) {
                return true;
            }
        }

        return false;
    }
}
