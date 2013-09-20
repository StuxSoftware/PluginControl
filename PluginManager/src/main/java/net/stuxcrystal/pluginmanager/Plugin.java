package net.stuxcrystal.pluginmanager;

import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;

import java.io.File;
import java.util.List;

/**
 * Represents a single plugin.
 *
 * @param <T> The base type of the plugin.
 */
public abstract class Plugin<T> implements Comparable<Plugin<T>> {

    /**
     * @return The name of the plugin.
     */
    public abstract String getName();

    /**
     * @return The file to the plugin. (Usually a jar file.)
     */
    public abstract File getFile();

    /**
     * @return The description.
     */
    public abstract String getDescription();

    /**
     * @return The main class of the plugin.
     */
    public abstract String getMainClass();

    /**
     * @return A list of authors.
     */
    public abstract List<String> getAuthors();

    /**
     * @return Returns the version.
     */
    public abstract String getVersion();

    /**
     * @return The current state of the plugin.
     */
    public abstract PluginState getState();

    /**
     * Returns a list of plugins that depends on that plugin.
     *
     * @param soft Retrieve optional dependencies.
     * @return A list of plugin names.
     */
    public abstract List<String> getDependencies(boolean soft);

    /**
     * @return The underlying object.
     */
    public abstract T getHandle();

    /**
     * Enables the plugin.<p />
     * <p/>
     * Does nothing if the plugin is enabled already.
     */
    public abstract void enable() throws PluginManagerException;

    /**
     * Disables a plugin without unloading it.<p />
     * <p/>
     * Does nothing if the plugin is not loaded or is not enabled.
     */
    public abstract void disable() throws PluginManagerException;

    /**
     * Loads the plugin.<p />
     * <p/>
     * Does nothing if the plugin is already loaded.
     */
    public abstract void load() throws PluginManagerException;

    /**
     * Unloads the plugin.<p />
     * <p/>
     * Does nothing if the plugin is not loaded.
     */
    public abstract void unload() throws PluginManagerException;

    /**
     * @return The object that should be used to check if the two plugins are equal.
     */
    protected Object compareObject() {
        return this.getHandle();
    }

    /**
     * Check if the two plugins are equal.
     * @param object The other object.
     * @return
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plugin<?>)) return false;
        Object me = this.compareObject();
        Object other = ((Plugin) object).compareObject();

        if (me == null || other == null) return false;
        return me.equals(other);
    }

    /**
     * Compares two plugins.
     * @param plugin The other plugin.
     * @return
     */
    @Override
    public int compareTo(Plugin<T> plugin) {
        return this.getName().compareTo(plugin.getName());
    }

    /**
     * Returns the string representation of the plugin (&lt;Name&gt v&lt;Version&gt;)
     * @return A string representing this plugin.
     */
    @Override
    public String toString() {
        return this.getName();
    }

}
