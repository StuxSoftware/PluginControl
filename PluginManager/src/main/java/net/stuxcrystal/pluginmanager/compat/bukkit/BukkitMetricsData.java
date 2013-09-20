package net.stuxcrystal.pluginmanager.compat.bukkit;

import net.stuxcrystal.pluginmanager.metrics.MetricsMetadata;
import org.bukkit.Bukkit;

/**
 * Implements the data of plugin-metrics.
 */
public class BukkitMetricsData implements MetricsMetadata {

    @Override
    public String getServerVersion() {
        return Bukkit.getVersion();
    }

    @Override
    public int getPlayerCount() {
        return Bukkit.getOnlinePlayers().length;
    }
}
