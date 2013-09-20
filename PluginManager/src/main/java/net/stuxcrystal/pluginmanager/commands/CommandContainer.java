package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.pluginmanager.PluginManager;

/**
 * Container for commands
 */
public abstract class CommandContainer {

    private final PluginManager manager;

    public CommandContainer(PluginManager manager) {
        this.manager = manager;
    }

    protected PluginManager getPluginManager() {
        return this.manager;
    }

    protected String _(String key, String... format) {
        return this.manager.translate(key, format);
    }

}
