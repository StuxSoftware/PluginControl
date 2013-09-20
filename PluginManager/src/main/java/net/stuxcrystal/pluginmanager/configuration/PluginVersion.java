package net.stuxcrystal.pluginmanager.configuration;

import net.stuxcrystal.configuration.annotations.Configuration;

/**
 * Configuration about the version of the plugin.
 */
@Configuration
public class PluginVersion {

    /**
     * The name of the plugin.
     */
    private String name = "UNKNOWN (Failed to parse.)";

    /**
     * The version of the plugin.
     */
    private String version = "UNKNOWN (Failed to parse.)";

    /**
     * @return The name of the plugin.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The version of the plugin.
     */
    public String getVersion() {
        return this.version;
    }

}
