package net.stuxcrystal.pluginmanager.compat.canary;

import net.canarymod.Canary;
import net.stuxcrystal.pluginmanager.metrics.MetricsMetadata;

/**
 * The data for metrics.
 */
public class RecodeMetricsData implements MetricsMetadata {


    @Override
    public String getServerVersion() {
        return "Recode " + Canary.getServer().getCanaryModVersion() + "(MC: " + Canary.getServer().getServerVersion() + ")";
    }

    @Override
    public int getPlayerCount() {
        return Canary.getServer().getNumPlayersOnline();
    }
}
