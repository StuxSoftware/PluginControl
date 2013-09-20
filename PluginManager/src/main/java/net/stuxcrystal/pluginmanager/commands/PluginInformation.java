package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.Backend;
import net.stuxcrystal.commandhandler.Command;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.PluginState;
import net.stuxcrystal.pluginmanager.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Information about plugins.
 */
public class PluginInformation extends CommandContainer {


    public PluginInformation(PluginManager manager) {
        super(manager);
    }

    @Command(value = "list", maxSize = 1, permission = "pluginmanager.info.list")
    public void list(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        int page = arguments.getInt(0, 1);

        List<String> lines = new ArrayList<String>();
        Plugin[] plugins = this.getPluginManager().getPlugins();
        Arrays.sort(plugins);
        for (Plugin plugin : plugins) {

            PluginState state = plugin.getState();

            if (state == PluginState.UNLOADED && !sender.hasPermission("pluginmanager.info.list.unloaded"))
                continue;
            else if (state == PluginState.ENABLED && !sender.hasPermission("pluginmanager.info.list.enabled"))
                continue;
            else if (state == PluginState.DISABLED && !sender.hasPermission("pluginmanager.info.list.disabled"))
                continue;
            else if (state == PluginState.UNKNOWN && !sender.hasPermission("pluginmanager.info.list.unknown"))
                continue;

            StringBuilder line = new StringBuilder();

            switch (state) {
                case ENABLED:
                    line.append(_("info.list.state.enabled"));
                    break;
                case DISABLED:
                    line.append(_("info.list.state.disabled"));
                    break;
                case UNLOADED:
                    line.append(_("info.list.state.unloaded"));
                    break;
                case UNKNOWN:
                    line.append(_("info.list.state.unknown"));
                    break;
            }

            line.append(_("info.list.line", plugin.getName(), plugin.getFile().getName()));
            lines.add(line.toString());
        }

        String[] lineArray = lines.toArray(new String[lines.size()]);
        this.getPluginManager().getUserInterface().printPaginated(sender, "info.list.header", page, lineArray);
    }

    @Command(value="info", minSize = 1, maxSize = 1, permission = "pluginmanager.info.info")
    public void info(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));

        if (plugin == null) {
            sender.sendMessage(_("info.info.notfound", arguments.getString(0)));
            return;
        }
        if (!sender.hasPermission("pluginmanager.info.info." + plugin.getState().name().toLowerCase())) {
            sender.sendMessage(_("cmd.check.permission"));
            return;
        }

        List<String> lines = new ArrayList<String>();

        // NAME
        lines.add(_("info.info.line.name", plugin.getName()));
        lines.add(_("info.info.line.version", plugin.getVersion()));
        lines.add(_("info.info.line.description", plugin.getDescription()));
        lines.add(getJoinedString(plugin.getAuthors(), "info.info.line.author"));
        lines.add(_("info.info.line.state", _("info.info.state." + plugin.getState().name().toLowerCase())));
        lines.add(_("info.info.line.file", plugin.getFile().getName()));
        lines.add(_("info.info.line.main", plugin.getMainClass()));
        lines.add(getJoinedString(plugin.getDependencies(false), "info.info.line.dependencies.hard"));
        lines.add(getJoinedString(plugin.getDependencies(true), "info.info.line.dependencies.soft"));

        String[] messages = lines.toArray(new String[lines.size()]);
        this.getPluginManager().getUserInterface().printUnpaginated(sender, "info.info.header", messages);
    }

    @Command(value="query", minSize = 1, maxSize = 1, permission = "pluginmanager.info.query", async = true)
    public void query(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) {
        net.stuxcrystal.pluginmanager.repository.PluginInformation information =
                this.getPluginManager().getRepositoryManager().getPluginInformation(arguments.getString(0));

        if (information == null) {
            sender.sendMessage(_("info.info.notfound", arguments.getString(0)));
            return;
        }

        List<String> lines = new ArrayList<>();

        lines.add(_("info.query.name", information.getName()));
        lines.add(_("info.query.version", information.getVersion()));
        lines.add(_("info.query.description", information.getDescription()));
        lines.add(getJoinedString(information.getAuthors(), "info.query.authors"));
        lines.add(getJoinedString(information.getDependencies(), "info.query.dependencies"));
        lines.add(_("info.query.repository", information.getRepository().getName()));

        String[] messages = lines.toArray(new String[lines.size()]);
        this.getPluginManager().getUserInterface().printUnpaginated(sender, "info.query.header", messages);
    }

    /**
     * Joins a list of strings making it translatable.
     * @param strings List of strings.
     * @param baseKey The base translation key.
     * @return The result value.
     */
    private String getJoinedString(List<String> strings, String baseKey) {
        if (strings.size() == 0 || strings == null) {
            return _(baseKey + ".none");
        } else if (strings.size() == 1) {
            return _(baseKey + ".one", strings.get(0));
        } else {
            return _(baseKey + ".multi", StringUtil.join(strings, _("misc.listseparator")));
        }
    }
}
