package net.stuxcrystal.pluginmanager.repository;

import net.stuxcrystal.pluginmanager.ServerPlatform;

/**
 * Repository Handler.
 */
public abstract class Repository<T> {

    /**
     * Reference to the settings object.
     */
    protected T settings;

    /**
     * The name should not contain any special character that is forbidden inside
     * filenames.
     * @return The name of the repository.
     */
    public abstract String getName();

    /**
     * @return The description of the repository.
     */
    public abstract String getDescription();

    /**
     * Checks if the repository is compatible with this platform.
     * @param platform The platform the plugin is running on.
     * @return A server platform.
     */
    public abstract boolean isCompatible(ServerPlatform platform);

    /**
     *
     * @return A class-object or null if this repository does not provide any settings.
     */
    public abstract Class<T> getSettingsClass();

    /**
     * Stores the settings.
     * @param settings The settings of the repository.
     */
    public void setSettings(Object settings) {
        this.settings = (T) settings;
    }

    /**
     * @return The settings of this repository.
     */
    public T getSettings() {
        return this.settings;
    }

    /**
     * Retrieves the information about the plugin.
     * @param name The name of the plugin.
     * @return A {@link PluginInformation}-Instance containing the meta-data of the plugin.
     */
    public abstract PluginInformation getPluginInformation(String name);


}
