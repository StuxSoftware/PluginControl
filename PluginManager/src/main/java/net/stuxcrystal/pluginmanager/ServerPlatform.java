package net.stuxcrystal.pluginmanager;

import net.stuxcrystal.commandhandler.CommandHandler;
import net.stuxcrystal.pluginmanager.metrics.MetricsMetadata;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Platform of the server.
 */
public interface ServerPlatform {

    /**
     * The name of the platform, e.g. Bukkit.
     *
     * @return A string containing the name of the platform.
     */
    public String getPlatformName();

    /**
     * The version of the platform. Used to provide information using
     * the User-Agent.
     *
     * @return A string containing the platform version.
     */
    public String getPlatformVersion();

    /**
     * Returns the java.util.Logger of the platform.
     *
     * @return The logging platform.
     */
    public Logger getLogger();

    /**
     * @return the current plugin directory.
     */
    public File getPluginDirectory();

    /**
     * @return the configuration directory.
     */
    public File getConfigurationDirectory();

    /**
     * @return A list of plugins.
     */
    public Plugin[] getPlugins();

    /**
     * @param name The name of a plugin.
     * @return A plugin wrapper if the plugin was found. "null" otherwise.
     */
    public Plugin getPlugin(String name);

    /**
     * A commandhandler handles all commands in a platform independent manner..
     *
     * @return A commandhandler.
     */
    public CommandHandler getCommands();

    /**
     * Copies the default resource file to the plugin directory.
     *
     * @param from The source file.
     * @param to   The destination file.
     */
    public void copyResource(String from, String to);

    /**
     * Returns the resource as a stream.
     *
     * @param name The name of the resource.
     * @return An input stream connected to that resouce.
     */
    public InputStream getResource(String name);

    /**
     * Reloads the plugin.
     */
    public void reload();

    /**
     * Checks if the file is a valid plugin.
     * @param file The file to check.
     * @return true if the file is a valid plugin.
     */
    public boolean isValidPlugin(File file);

    /**
     * Checks if the filename marks a valid plugin.
     * @param filename The file to check.
     * @return true if the file is a valid plugin.
     */
    public boolean isValidPlugin(String filename);

    /**
     * Parses the plugin-file to obtain its meta-data.<p />
     *
     * Please note that this function is a potential bug source,
     * because the plugin instance provided can be activated without
     * being registered to the plugin-list.
     *
     * @param file The file to activate.
     * @return The plugin instance.
     */
    public Plugin parseMeta(File file);

    /**
     * Schedules a synchronous task.
     * @param runnable Task to schedule.
     */
    public void schedule(Runnable runnable);

    /**
     * Returns the statistics about the server.
     * @return
     */
    public MetricsMetadata getStatistics();

}
