package net.stuxcrystal.pluginmanager.compat.bukkit;

import net.stuxcrystal.commandhandler.CommandHandler;
import net.stuxcrystal.commandhandler.compat.bukkit.BukkitCommandHandler;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.ServerPlatform;
import net.stuxcrystal.pluginmanager.io.FileUtils;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.metrics.MetricsMetadata;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * The functions currently needed for the interoperability of the server platform.
 */
public class BukkitServerPlatform extends JavaPlugin implements ServerPlatform {

    /**
     * The command handler handles all commands.
     */
    private BukkitCommandHandler commands;

    /**
     * The plugin manager is the internal plugin entry point.
     */
    private PluginManager manager;

    /**
     * All objects are initialized on load.
     */
    @Override
    public void onLoad() {
        commands = new BukkitCommandHandler(this);
        manager = new PluginManager(this);

        manager.load();
    }

    @Override
    public void onEnable() {
        getServer().getPluginCommand("pman").setExecutor(commands);
        manager.enable();
    }

    @Override
    public void onDisable() {
        manager.disable();
    }

    @Override
    public String getPlatformName() {
        return "Bukkit";
    }

    @Override
    public String getPlatformVersion() {
        return Bukkit.getBukkitVersion();
    }

    @Override
    public File getPluginDirectory() {
        return FileUtils.toAbsoluteCanonicalFile(this.getFile().getParentFile());
    }

    @Override
    public File getConfigurationDirectory() {
        return this.getDataFolder();
    }

    @Override
    public Plugin[] getPlugins() {
        List<File> jarFiles = BukkitUtil.getJarFiles(getPluginDirectory(), this.getPluginLoader());
        List<Plugin<PluginDescriptionFile>> plugins = new ArrayList<Plugin<PluginDescriptionFile>>();

        for (File jarFile : jarFiles) {
            Plugin plugin = this.parseMeta(jarFile, true);
            if (plugin != null) plugins.add(plugin);
        }

        return plugins.toArray(new Plugin[plugins.size()]);
    }

    @Override
    public Plugin getPlugin(String name) {
        Plugin[] plugins = getPlugins();

        for (Plugin plugin : plugins) {
            if (plugin.getName().equalsIgnoreCase(name))
                return plugin;
        }

        return null;
    }

    @Override
    public CommandHandler getCommands() {
        return commands;
    }

    @Override
    public void copyResource(String from, String to) {
        InputStream is = this.getResource(from);
        try {
            StreamUtilities.move(is, new File(this.getConfigurationDirectory(), to));
        } catch (IOException e) {
            this.getLogger().log(Level.WARNING, "[Backend] Failed to copy resource.", e);
        } finally {
            try { is.close(); } catch (IOException ingored) { }
        }
    }

    @Override
    public void reload() {
        // Does nothing currently.
    }

    @Override
    public boolean isValidPlugin(File file) {
        return BukkitUtil.isValidFile(file, this.getPluginLoader());
    }

    @Override
    public boolean isValidPlugin(String filename) {
        return BukkitUtil.isValidFile(filename, this.getPluginLoader());
    }

    @Override
    public Plugin parseMeta(File file) {
        return parseMeta(file, this.manager.getConfiguration().isVerbose());
    }

    @Override
    public void schedule(Runnable runnable) {
        this.getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public MetricsMetadata getStatistics() {
        return new BukkitMetricsData();
    }

    public Plugin parseMeta(File file, boolean warn) {
        try {
            return new BukkitPlugin(file, getServer().getPluginManager(), this.getPluginLoader());
        } catch (InvalidDescriptionException e) {
            if (warn) this.getLogger().log(Level.SEVERE, "[Backend] Failed to wrap plugin: " + file.getName(), e);
            return null;
        }
    }

    /**
     * Redirects the commands to the CommandHelper library.
     *
     * @param sender  The sender who sent the command.
     * @param command The command that is executed.
     * @param label   The label of the command.
     * @param args    The arguments of the command.
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commands.onCommand(sender, command, label, args);
    }
}
