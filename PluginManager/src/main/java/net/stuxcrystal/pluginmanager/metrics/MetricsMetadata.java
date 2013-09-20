package net.stuxcrystal.pluginmanager.metrics;

/**
 * The metadata for PluginMetrics.
 */
public interface MetricsMetadata {

    /**
     * The server version.
     * @return A string containing the server version.
     */
    public String getServerVersion();

    /**
     * Returns the count of players.
     * @return The count of players.
     */
    public int getPlayerCount();

}
