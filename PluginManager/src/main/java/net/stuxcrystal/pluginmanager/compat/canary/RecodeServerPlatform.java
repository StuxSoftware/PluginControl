package net.stuxcrystal.pluginmanager.compat.canary;

import net.canarymod.Canary;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.tasks.ServerTask;
import net.stuxcrystal.commandhandler.CommandHandler;
import net.stuxcrystal.commandhandler.compat.canary.CanaryCommandHandler;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.ServerPlatform;
import net.stuxcrystal.pluginmanager.io.FileUtils;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.metrics.MetricsMetadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-Platform for Canary-Recode.
 */
public class RecodeServerPlatform extends net.canarymod.plugin.Plugin implements ServerPlatform, CommandListener {

    PluginManager pluginManager;

    CanaryCommandHandler handler;

    @Override
    public String getPlatformName() {
        return "Canary Recode";
    }

    @Override
    public String getPlatformVersion() {
        return Canary.getServer().getCanaryModVersion();
    }

    @Override
    public Logger getLogger() {
        return this.getLogman();
    }

    @Override
    public File getPluginDirectory() {
        return FileUtils.toAbsoluteCanonicalFile(new File(this.getJarPath(), ".."));
    }

    @Override
    public File getConfigurationDirectory() {
        return FileUtils.toAbsoluteCanonicalFile(new File("config", "PluginControl"));
    }

    @Override
    public Plugin[] getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();
        for (File f : this.getPluginDirectory().listFiles()) {
            if (!f.getName().endsWith(".jar")) continue;

            Plugin pluginInstance = this.parseMeta(f, true);
            if (pluginInstance == null) continue;

            plugins.add(pluginInstance);
        }

        return plugins.toArray(new Plugin[plugins.size()]);
    }

    @Override
    public Plugin getPlugin(String name) {
        for (Plugin plugin : this.getPlugins()) {
            if (plugin.getName().equals(name)) {
                return plugin;
            }
        }

        return null;
    }

    @Override
    public CommandHandler getCommands() {
        return handler;
    }

    @Override
    public void copyResource(String from, String to) {
        try {
            StreamUtilities.move(getResource(from), new File(getConfigurationDirectory(), to));
        } catch (IOException e) {
            this.getLogman().log(Level.WARNING, "[Backend] Failed to copy resource file.", e);
        }
    }

    @Override
    public InputStream getResource(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public void reload() {
        // Do nothing currently.
    }

    @Override
    public boolean isValidPlugin(File file) {
        return file.getName().endsWith(".jar");
    }

    @Override
    public boolean isValidPlugin(String filename) {
        return filename.endsWith(".jar");
    }

    @Override
    public Plugin parseMeta(File file) {
        return parseMeta(file, false);
    }

    @Override
    public void schedule(final Runnable runnable) {
        Canary.getServer().addSynchronousTask(new ServerTask(this, 0) {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public MetricsMetadata getStatistics() {
        return new RecodeMetricsData();
    }

    /**
     * Wraps the file into a plugin class.
     * @param file The file to wrap.
     * @param warn Should be a warning given if the file is not a jar-File.
     * @return a Plugin instance if the file is a valid plugin, null otherwise.
     */
    public Plugin parseMeta(File file, boolean warn) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            if (warn) getLogger().log(Level.WARNING, "[Backend] Failed to open plugin: " + file.getName(), e);
            return null;
        }

        boolean valid = jarFile.getEntry("Canary.inf") != null;
        try { jarFile.close(); } catch (IOException ignored) { }
        if (!valid) {
            return null;
        }

        return new RecodePlugin(file);
    }

    @Override
    public boolean enable() {
        this.handler = new CanaryCommandHandler(this);
        this.pluginManager = new PluginManager(this);

        this.pluginManager.load();
        this.pluginManager.enable();

        try {
            Canary.commands().registerCommands(this, this, true);
        } catch (CommandDependencyException e) {
            this.getLogger().severe("[Backend] Failed to register commands...");
        }

        return true;
    }

    @Override
    public void disable() {
        // Do nothing currently.
    }

    @Command(
            aliases = {"pman", "pluginmanager"},
            toolTip = "Base command for the plugin manager",
            permissions = {},
            description = "PluginManager - a platform independent way to manage your plugins.")
    public void pman(MessageReceiver receiver, String[] c) {
        this.handler.executeSubCommand(receiver, c);
    }
}
