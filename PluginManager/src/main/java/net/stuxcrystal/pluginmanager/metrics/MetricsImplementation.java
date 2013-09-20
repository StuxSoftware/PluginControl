package net.stuxcrystal.pluginmanager.metrics;

import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.configuration.PluginVersion;

import java.io.File;
import java.io.IOException;

/**
 * The Metrics
 */
public class MetricsImplementation extends Metrics {

    /**
     * The metadata of the server. (Provided by the ServerPlatform implementation)
     */
    MetricsMetadata metricsMetadata;

    /**
     * The version of the plugin. (This information is provided in a file in the jar)
     */
    PluginVersion version;

    /**
     * A reference to the plugin manager.
     */
    PluginManager manager;

    public MetricsImplementation(PluginManager manager, PluginVersion version, MetricsMetadata metricsMetadata)
            throws IOException {
        super(version.getName(), version.getVersion());

        this.metricsMetadata = metricsMetadata;
        this.version         = version;
        this.manager         = manager;
    }

    @Override
    public String getFullServerVersion() {
        return this.metricsMetadata.getServerVersion();
    }

    @Override
    public int getPlayersOnline() {
        return this.metricsMetadata.getPlayerCount();
    }

    @Override
    public File getConfigFile() {
        if (this.manager != null)
            return this.manager.getConfiguration().getMetricsConfiguration().getMetricsFile();
        else
            return PluginManager.getInstance().getConfiguration().getMetricsConfiguration().getMetricsFile();
    }
}
