package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.Backend;
import net.stuxcrystal.commandhandler.Command;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlling the backends.
 */
public class BackendControl extends CommandContainer {

    public BackendControl() {
        super(PluginManager.getInstance());
    }

    @Command(value = "reload", permission = "pluginmanager.admin.reload")
    public void reload(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        if (sender.isPlayer())
            backend.getLogger().info("[Command] Player \"" + sender.getName() + "\" reloads the plugin.");

        this.getPluginManager().reload();
        sender.sendMessage(this.getPluginManager().translate("admin.reload.success"));
    }

    @Command(value = "reset", permission = "pluginmanager.admin.reset")
    public void reset(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        if (sender.isPlayer())
            backend.getLogger().info("[Command] Player \"" + sender.getName() + "\" resets the configuration.");

        this.getPluginManager().reset();
        sender.sendMessage(this.getPluginManager().translate("admin.reset.success"));
    }

    @Command(value = "update", permission = "pluginmanager.admin.update")
    public void updateConfiguration(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        if (sender.isPlayer())
            backend.getLogger().info("[Command] Player \"" + sender.getName() + "\" updates the configuration.");


    }

    @Command(value = "repositories", permission = "pluginmanager.admin.repositories", minSize = 0, maxSize = 1)
    public void listRepositories(Backend backend, CommandExecutor sender, ArgumentParser arguments) {
        if (sender.isPlayer())
            backend.getLogger().info("[Command] Player \"" + sender.getName() + "\" lists all repositories.");

        int page = arguments.getInt(0, 1);

        List<String> repositories = new ArrayList<String>();
        for (Repository repo : this.getPluginManager().getRepositoryManager().getRepositories()) {
            repositories.add(_("admin.repositories.line", repo.getName(), repo.getDescription()));
        }

        this.getPluginManager().getUserInterface().printPaginated(
                sender,
                "admin.repositories.header",
                page,
                repositories.toArray(new String[repositories.size()])
        );
    }
}
