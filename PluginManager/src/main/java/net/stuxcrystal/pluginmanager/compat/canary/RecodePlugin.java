package net.stuxcrystal.pluginmanager.compat.canary;

import net.canarymod.Canary;
import net.canarymod.CanaryClassLoader;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginLoader;
import net.stuxcrystal.pluginmanager.PluginState;
import net.stuxcrystal.pluginmanager.exceptions.IOOperationException;
import net.stuxcrystal.pluginmanager.exceptions.PluginException;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;
import net.stuxcrystal.pluginmanager.io.FileUtils;
import net.stuxcrystal.pluginmanager.utils.ReflectionUtil;
import net.visualillusionsent.utils.PropertiesFile;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a recode plugin.
 */
public class RecodePlugin extends net.stuxcrystal.pluginmanager.Plugin<PropertiesFile> {

    final PropertiesFile properties;

    final File file;

    public RecodePlugin(File file) {
        this.file = FileUtils.toAbsoluteCanonicalFile(file);
        this.properties = new PropertiesFile(file.getAbsolutePath(), "Canary.inf");
    }

    @Override
    public String getName() {
        if (!this.properties.containsKey("name"))
            return simpleMain(this.properties.getString("main-class"));

        return this.properties.getString("name");
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getDescription() {
        return this.properties.getString("description", "");
    }

    @Override
    public String getMainClass() {
        return this.properties.getString("main-class");
    }

    @Override
    public List<String> getAuthors() {
        return Arrays.asList(this.properties.getString("author", ""));
    }

    @Override
    public String getVersion() {
        return this.properties.getString("version", "");
    }

    @Override
    public PluginState getState() {
        Plugin plugin = Canary.loader().getPlugin(this.getName());
        if (plugin == null || plugin.isClosed())
            return PluginState.UNLOADED;

        return plugin.isDisabled() ? PluginState.DISABLED : PluginState.ENABLED;
    }

    /**
     * Retrieves all dependencies.
     *
     * @param soft Retrieve optional dependencies.
     * @return A list of hard-dependencies.
     */
    @Override
    public List<String> getDependencies(boolean soft) {
        String node = (soft ? "optional-" : "") + "dependencies";
        if (!this.properties.containsKey(node)) return Collections.emptyList();
        return Arrays.asList(this.properties.getStringArray(node, "[,;]+"));
    }

    @Override
    public PropertiesFile getHandle() {
        return this.properties;
    }

    @Override
    public void enable() throws PluginManagerException {
        if (getState() == PluginState.ENABLED) return;

        if (getState() == PluginState.UNLOADED)
            load();

        Canary.loader().enablePlugin(this.getName());

    }

    @Override
    public void disable() throws PluginManagerException {
        if (getState() != PluginState.ENABLED) return;

        Canary.loader().disablePlugin(this.getName());
    }

    @Override
    public void load() throws PluginManagerException {
        if (getState() != PluginState.UNLOADED) return;

        try {
            if (!ReflectionUtil.<Boolean>invokeQuiet(PluginLoader.class, Canary.loader(), new String[]{"load"}, this.file))
                throw new IOOperationException("Load operation failed of plugin: " + this.getName());

        } catch (IOOperationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unload() throws PluginManagerException {
        if (getState() == PluginState.UNLOADED) return;

        String name = this.getName();
        Plugin plugin = Canary.loader().getPlugin(name);

        if (getState() == PluginState.ENABLED)
            disable();

        PluginLoader loader = Canary.loader();

        Map<String, Plugin> plugins = ReflectionUtil.getFieldQuiet(PluginLoader.class, loader, "plugins");

        if (plugins == null)
            throw new PluginException("Failed to access plugin lists...");

        try {
            ReflectionUtil.invokeQuiet(Plugin.class, plugin, new String[]{"markClosed"});
        } catch (Throwable e) {
            throw new PluginException("Unexcpected exception while preparation of plugin unload:", e);
        }

        // This is the unload magic.
        plugins.remove(name);
        ((CanaryClassLoader) plugin.getClass().getClassLoader()).close();

    }

    /**
     * Returns the name of the class.
     *
     * @param name The name of the class.
     * @return A string containing the name of the class.
     */
    private final String simpleMain(String name) {
        if (!name.contains(".")) return name;
        return name.substring(name.lastIndexOf(".") + 1);
    }

    protected Object compareObject() {
        return this.getFile();
    }
}
