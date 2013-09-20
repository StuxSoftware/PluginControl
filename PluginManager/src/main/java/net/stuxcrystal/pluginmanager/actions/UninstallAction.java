package net.stuxcrystal.pluginmanager.actions;

import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;
import net.stuxcrystal.pluginmanager.utils.DependencyUtils;

import static net.stuxcrystal.pluginmanager.PluginManager._;

import java.util.List;

/**
 * Uninstalls a plugin.
 */
public class UninstallAction {

    /**
     * The PluginManager handling the dependencies.
     */
    private final PluginManager manager;

    /**
     * Initializes the uninstaller.
     * @param manager The plugin manager handling the manager.
     */
    public UninstallAction(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Uninstalls a plugins and all hard-dependencies.
     * @param plugin   The plugin to install.
     * @param executor The sender that executed the command.
     * @param verbose  Should the output be verbose.
     * @throws PluginManagerException If an internal action fails.
     */
    public void uninstallPlugin(Plugin plugin, CommandExecutor executor, boolean verbose)
            throws PluginManagerException {

        if (this.manager.getConfiguration().isVerbose()) verbose = true;

        executor.sendMessage(_("install.uninstall.stage.dependencies"));

        // Plugins to remove.
        List<Plugin> harddependencies = DependencyUtils.getDependentPluginTree(plugin, false);
        List<Plugin> softdependencies = DependencyUtils.getDependentPluginTree(plugin, true);

        // Remove dependencies.
        for (Plugin harddependency : harddependencies)
            softdependencies.remove(harddependency);

        // Removes the plugin from the list.
        if (!harddependencies.contains(plugin)) harddependencies.add(plugin);
        softdependencies.remove(plugin);

        executor.sendMessage(_("install.uninstall.stage.unload"));

        // Unloads the plugin.
        for (Plugin softdependency : softdependencies) {
            if (verbose) executor.sendMessage(_("install.uninstall.stage.unload.verbose", softdependency));
            softdependency.disable();
            softdependency.unload();
        }
        for (Plugin dependency : harddependencies) {
            if (verbose) executor.sendMessage(_("install.uninstall.stage.unload.verbose", dependency));
            dependency.disable();
            dependency.unload();
        }

        // Executing garbage collector to enforce closing all the handles to the file.
        System.gc();

        // Delete all plugins and all plugins that depends on the given plugin.
        executor.sendMessage(_("install.uninstall.stage.remove"));
        for (Plugin dependency : harddependencies) {
            if (verbose) executor.sendMessage(_("install.uninstall.stage.remove.verbose", dependency));
            this.manager.getLogger().info("[Uninstall] Removing: " + dependency.getName());

            // Hacks to allow deleting of the file.
            dependency.getFile().setWritable(true);

            // Delete the file and report if it fails.
            if (!dependency.getFile().delete()) {
                this.manager.getLogger().warning("Failed to delete: " + dependency.getFile().getName());
                executor.sendMessage(_("install.uninstall.stage.remove.failed", dependency));
            }
        }

        // Reload all soft-dependencies.
        executor.sendMessage(_("install.uninstall.stage.load"));
        for (Plugin dependency : softdependencies) {
            if (verbose) executor.sendMessage(_("install.uninstall.stage.load.verbose", dependency));
            dependency.load();
            dependency.enable();
        }

    }

}
