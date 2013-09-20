package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.commandhandler.*;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.configuration.PluginVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * All subcommands.
 */
public class GeneralCommands extends CommandContainer {

    public GeneralCommands(PluginManager manager) {
        super(manager);
    }

    @Command(value = "admin", permission = "pluginmanager.admin")
    @SubCommand(BackendControl.class)
    public void adminCommands(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {}

    @Command(value = " ", permission = "pluginmanager.admin")
    public void baseCommand(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        PluginVersion version = this.getPluginManager().getVersion();
        sender.sendMessage(
                _("plugin.command.base.line[0]", version.getName(), version.getVersion()),
                _("plugin.command.base.line[1]")
        );
    }

    @Command(value = "help", permission = "pluginmanager.help", maxSize = 1)
    public void help(Backend backend, CommandExecutor sender, ArgumentParser arguments) {
        int page = arguments.getInt(0, 1);

        List<String> lines = new ArrayList<String>();

        for (Command command : this.getPluginManager().getPlatform().getCommands().getDescriptors()) {
            if (command.value().trim().isEmpty()) continue;

            lines.add(_("plugin.help.line", command.value(), _("plugin.command." + command.value())));
        }

        this.getPluginManager().getUserInterface().printPaginated(
                sender,
                "plugin.help.header",
                page,
                lines.toArray(new String[lines.size()])
        );
    }

}
