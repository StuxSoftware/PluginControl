package net.stuxcrystal.pluginmanager.metrics;

import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.utils.ReflectionUtil;

import java.io.IOException;
import java.util.logging.Level;

/**
 * The MCStats implementation
 */
public class MCStatsImplementation implements PluginMetricsImplementation {

    MetricsImplementation implementation;

    @Override
    public void initialize(PluginManager manager) {
        try {
            implementation = new MetricsImplementation(manager, manager.getVersion(), manager.getPlatform().getStatistics());
        } catch (IOException e) {
            manager.getLogger().log(Level.WARNING, "[Metrics] Failed to initialize metrics.", e);
        }
    }

    @Override
    public void start() {
        if (implementation == null) return;
        implementation.start();
    }

    @Override
    public void stop() {
        // Check if Metrics is enabled.
        if (implementation == null) return;

        // Get the metrics thread.
        Thread thread = ReflectionUtil.getFieldQuiet(Metrics.class, implementation, "thread");

        // Plugin-Metrics already disabled.
        if (thread == null) return;

        // Stop plugin-metrics.
        ReflectionUtil.setFieldQuiet(Metrics.class, implementation, null, "thread");
        thread.interrupt();
    }
}
