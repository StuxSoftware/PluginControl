package net.stuxcrystal.pluginmanager;

/**
 * The state of the plugin.
 */
public enum PluginState {

    /**
     * Returned when the plugin is not loaded.
     */
    UNLOADED,

    /**
     * Used when the plugin is disabled but loaded.
     */
    DISABLED,

    /**
     * Used when the plugin is enabled.
     */
    ENABLED,

    /**
     * Used if the state couldn't be retrieved.
     */
    UNKNOWN;

}
