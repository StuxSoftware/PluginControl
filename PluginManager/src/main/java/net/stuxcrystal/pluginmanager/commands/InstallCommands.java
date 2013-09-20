package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.Backend;
import net.stuxcrystal.commandhandler.Command;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.exceptions.DependencyException;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;

/**
 * Commands to provide simple installation of commands.
 */
public class InstallCommands extends CommandContainer {

    public InstallCommands(PluginManager manager) {
        super(manager);
    }

    @Command(value="uninstall", minSize = 1, permission = "pluginmanager.install.uninstall", flags = "v",
             aliases = {"remove", "delete"}
    )
    public void uninstall(Backend backend, CommandExecutor executor, ArgumentParser parser) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(parser.getString(0));

        if (executor.isPlayer()) {
            backend.getLogger().info(
                    String.format("[Command] Player {0} uninstalls {1}", executor, plugin)
            );
        }

        boolean verbose = parser.hasFlag('v');
        this.getPluginManager().getActions().getUninstaller().uninstallPlugin(plugin, executor, verbose);
        executor.sendMessage(_("install.uninstall.command.success"));
    }

    @Command(value="install", minSize = 1, permission = "pluginmanager.install.install", flags="vdl", async = true)
    public void install(Backend backend, CommandExecutor executor, ArgumentParser parser) throws PluginManagerException {
        String name = parser.getString(0);

        if (executor.isPlayer()) {
            backend.getLogger().info(
                    String.format("[Command] Player {0} installs {1}", executor, name)
            );
        }

        boolean verbose = parser.hasFlag('v');
        boolean dry     = parser.hasFlag('d');
        boolean load    = parser.hasFlag('l');

        if (dry && load) {
            executor.sendMessage(_("install.install.command.load-and-dry"));
            return;
        }

        try {
            this.getPluginManager().getActions().getInstaller().install(name, executor, verbose, dry, load);
        } catch (DependencyException e) {
            executor.sendMessage(_("install.install.command.notfound"));
            return;
        }
        executor.sendMessage(_("install.install.command.success"));
    }

    @Command(value="update", minSize = 1, permission = "pluginmanager.install.update", flags="v", async = true)
    public void update(Backend backend, CommandExecutor executor, ArgumentParser parser) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(parser.getString(0));

        if (plugin == null) {
            executor.sendMessage(_("install.install.command.notfound"));
            return;
        }

        if (executor.isPlayer()) {
            backend.getLogger().info(
                    String.format("[Command] Player {0} updates {1}", executor, plugin.getName())
            );
        }

        boolean verbose = parser.hasFlag('v');

        this.getPluginManager().getActions().getUpdater().update(executor, new Plugin[]{plugin}, verbose);
        executor.sendMessage(_("install.update.command.success"));
    }
}
