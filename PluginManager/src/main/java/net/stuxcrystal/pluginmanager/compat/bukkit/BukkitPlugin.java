package net.stuxcrystal.pluginmanager.compat.bukkit;

import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginState;
import net.stuxcrystal.pluginmanager.exceptions.FrontendException;
import net.stuxcrystal.pluginmanager.exceptions.PluginException;
import net.stuxcrystal.pluginmanager.utils.ReflectionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Proxy for a plugin.
 */
public class BukkitPlugin extends Plugin<PluginDescriptionFile> {

    /**
     * The path to the plugin.
     */
    private final File pluginFile;

    /**
     * The description file of the plugin.
     */
    private final PluginDescriptionFile plugin;

    /**
     * The loader of the java plugins.
     */
    private final PluginLoader loader;

    /**
     * The manager of the server.
     */
    private final PluginManager manager;

    /**
     * Initializes the plugin.
     *
     * @param pluginFile The path to the plugin.
     * @param manager    The plugin manager that loaded the plugin.
     * @param loader     The loader of the plugin.
     * @throws InvalidDescriptionException Thrown when the plugin description is invalid.
     */
    public BukkitPlugin(File pluginFile, PluginManager manager, PluginLoader loader) throws InvalidDescriptionException {
        this.pluginFile = pluginFile;
        this.plugin = loader.getPluginDescription(this.pluginFile);

        this.loader = loader;
        this.manager = manager;
    }


    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public File getFile() {
        return this.pluginFile;
    }

    @Override
    public String getDescription() {
        return this.plugin.getDescription();
    }

    @Override
    public String getMainClass() {
        return this.plugin.getMain();
    }

    @Override
    public List<String> getAuthors() {
        return this.plugin.getAuthors();
    }

    @Override
    public String getVersion() {
        return this.plugin.getVersion();
    }

    @Override
    public PluginState getState() {
        org.bukkit.plugin.Plugin _plugin = manager.getPlugin(this.getName());

        if (_plugin == null)
            return PluginState.UNLOADED;

        if (!(_plugin instanceof JavaPlugin))
            return PluginState.UNKNOWN;

        JavaPlugin plugin = (JavaPlugin) _plugin;

        return plugin.isEnabled() ? PluginState.ENABLED : PluginState.DISABLED;
    }

    @Override
    public List<String> getDependencies(boolean soft) {
        List<String> dependencies;
        if (soft)
            dependencies = this.plugin.getSoftDepend();
        else
            dependencies = this.plugin.getDepend();

        if (dependencies == null)
            dependencies = Collections.emptyList();

        return dependencies;
    }

    @Override
    public PluginDescriptionFile getHandle() {
        return this.plugin;
    }

    @Override
    public void enable() throws FrontendException {
        if (getState() == PluginState.UNLOADED) {
            this.load();
        }

        manager.enablePlugin(getRealPlugin());
    }

    @Override
    public void disable() {
        PluginState state = getState();

        if (state != PluginState.ENABLED) return;
        manager.disablePlugin(manager.getPlugin(this.getName()));
    }

    @Override
    public void load() throws FrontendException {

        if (getState() == PluginState.UNLOADED) {
            try {
                executeLoad(this.manager.loadPlugin(this.pluginFile));
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                throw new PluginException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * The default load command on bukkit.
     * @param plugin The plugin to load.
     */
    private static void executeLoad(org.bukkit.plugin.Plugin plugin) {
        plugin.getLogger().info("Loading" + plugin.getDescription().getFullName());
        plugin.onLoad();
    }

    @Override
    public void unload() throws FrontendException {
        PluginState state = getState();
        JavaPlugin plugin = getRealPlugin();
        if (state == PluginState.UNLOADED || state == PluginState.UNKNOWN) {
            return;
        }

        // Retrieve the classloader at least before the plugin is disable.
        PluginClassLoader classLoader = BukkitUtil.getPluginClassLoader(plugin, this.loader);

        // Disable the plugin gracefully before disabling it.
        if (state == PluginState.ENABLED)
            disable();

        plugin.getLogger().info("Unloading " + plugin.getDescription().getFullName());

        // Get the logger to log fails while unloading the plugin.
        Logger logger = plugin.getLogger();

        // Get the value needed to unload the plugin.
        Map<String, org.bukkit.plugin.Plugin> names = ReflectionUtil.getFieldQuiet(SimplePluginManager.class, this.manager, "lookupNames");
        List<org.bukkit.plugin.Plugin> plugins = ReflectionUtil.getFieldQuiet(SimplePluginManager.class, this.manager, "plugins");

        SimpleCommandMap commands = ReflectionUtil.getFieldQuiet(SimplePluginManager.class, this.manager, "commandMap");
        Map<String, Command> knownCommands = ReflectionUtil.getFieldQuiet(SimpleCommandMap.class, commands, "knownCommands");

        if (names == null || plugins == null || commands == null || knownCommands == null) {
            StringBuilder exception = new StringBuilder();
            exception.append("Failed to perform reflective operation: [");
            exception.append(names).append(",");
            exception.append(plugins).append(",");
            exception.append(commands).append(",");
            exception.append(knownCommands).append("]");
            throw new PluginException(exception.toString());
        }

        // Remove the plugin out of the lists.
        synchronized (this.manager) {
            names.remove(plugin.getName());
            plugins.remove(plugin);
        }

        // Remove the commands.
        synchronized (commands) {
            List<String> removableCommands = new ArrayList<String>();
            for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand command = (PluginCommand) entry.getValue();
                    if (command.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                        command.unregister(commands);
                        removableCommands.add(entry.getKey());
                    }
                }
            }

            for (String name : removableCommands)
                knownCommands.remove(name);
        }

        // Close the class-loader.
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.warning("Failed to close class-loader. Plugin not completely unloaded...");
        }
    }

    /**
     * @return Returns the plugin instance behind this proxy.
     */
    JavaPlugin getRealPlugin() {
        return (JavaPlugin) manager.getPlugin(this.getName());
    }

    /**
     * Internal function.
     * @return The object used to check if the two plugins are equal.
     */
    protected Object compareObject() {
        return this.getFile();
    }
}
