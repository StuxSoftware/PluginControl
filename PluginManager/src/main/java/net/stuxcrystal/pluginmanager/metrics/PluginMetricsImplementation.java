package net.stuxcrystal.pluginmanager.metrics;

import net.stuxcrystal.pluginmanager.PluginManager;

/**
 * This interface is made to make it simple to change the metrics code.
 */
public interface PluginMetricsImplementation {

    /**
     * Initializes the implementation.
     */
    public void initialize(PluginManager manager);

    /**
     * Starts the action of the implementation.
     */
    public void start();

    /**
     * Stops the action of the implementation.
     */
    public void stop();

}
