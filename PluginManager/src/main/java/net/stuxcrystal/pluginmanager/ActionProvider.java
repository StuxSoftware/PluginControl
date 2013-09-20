package net.stuxcrystal.pluginmanager;

import net.stuxcrystal.pluginmanager.actions.InstallAction;
import net.stuxcrystal.pluginmanager.actions.UninstallAction;
import net.stuxcrystal.pluginmanager.actions.UpdateAction;

/**
 * Provider for complex plugin actions.
 */
public class ActionProvider {

    /**
     * Reference to the plugin manager.
     */
    private final PluginManager manager;

    /**
     * Reference to the uninstaller.
     */
    private final UninstallAction uninstaller;

    /**
     * Reference to the installer.
     */
    private final InstallAction installer;

    /**
     * Reference to the updater.
     */
    private final UpdateAction updater;

    /**
     * Initiates the action provider.
     * @param manager
     */
    public ActionProvider(PluginManager manager) {
        this.manager = manager;

        this.uninstaller = new UninstallAction(this.manager);
        this.installer = new InstallAction(this.manager);
        this.updater = new UpdateAction(this.manager);
    }

    /**
     * @return The uninstaller.
     */
    public UninstallAction getUninstaller() {
        return this.uninstaller;
    }

    /**
     * @return The installer.
     */
    public InstallAction getInstaller() {
        return this.installer;
    }

    /**
     * @return The updater.
     */
    public UpdateAction getUpdater() {
        return this.updater;
    }

}
