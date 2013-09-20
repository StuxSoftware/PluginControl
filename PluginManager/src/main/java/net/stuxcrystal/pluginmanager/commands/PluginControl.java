package net.stuxcrystal.pluginmanager.commands;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.Backend;
import net.stuxcrystal.commandhandler.Command;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.PluginState;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;

/**
 * All commands that are controlling plugins.
 */
public class PluginControl extends CommandContainer {


    public PluginControl(PluginManager manager) {
        super(manager);
    }

    @Command(value = "enable", permission = "pluginmanager.control.enable", minSize = 1)
    public void enable(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));

        if (plugin == null) {
            sender.sendMessage(_("control.enable.notfound", arguments.getString(0)));
            return;
        }

        if (plugin.getState() == PluginState.ENABLED) {
            sender.sendMessage(_("control.enable.already", arguments.getString(0)));
            return;
        }

        if (plugin.getState() == PluginState.UNLOADED && !sender.hasPermission("pluginmanager.control.load")) {
            sender.sendMessage(_("cmd.check.permission"));
            return;
        }

        if (sender.isPlayer())
            backend.getLogger().info("[Command] \"" + sender.getName() + "\" enables the plugin: " + plugin.getName());

        if (plugin.getState() == PluginState.UNLOADED)
            plugin.load();

        plugin.enable();
        sender.sendMessage(_("control.enable.success", plugin.getName()));
    }

    @Command(value = "disable", permission = "pluginmanager.control.disable", flags = "s", minSize = 1)
    public void disable(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));

        if (plugin == null) {
            sender.sendMessage(_("control.disable.notfound", arguments.getString(0)));
            return;
        }

        if (plugin.getState() != PluginState.ENABLED) {
            sender.sendMessage(_("control.disable.already", arguments.getString(0)));
            return;
        }

        if (sender.isPlayer())
            backend.getLogger().info("[Command] \"" + sender.getName() + "\" disables the plugin: " + plugin.getName());


        plugin.disable();
        if (!arguments.hasFlag('s') && sender.hasPermission("pluginmanager.control.unload"))
            plugin.unload();

        sender.sendMessage(_("control.disable.success", plugin.getName()));
    }

    @Command(value = "load", permission = "pluginmanager.control.load", minSize = 0)
    public void load(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));
        if (plugin == null) {
            sender.sendMessage(_("control.load.notfound", arguments.getString(0)));
            return;
        }

        if (plugin.getState() != PluginState.UNLOADED) {
            sender.sendMessage(_("control.load.loaded", arguments.getString(0)));
            return;
        }

        if (sender.isPlayer())
            backend.getLogger().info("[Command] \"" + sender.getName() + "\" loads the plugin: " + plugin.getName());


        plugin.load();
        sender.sendMessage(_("control.load.success", plugin.getName()));
    }

    @Command(value = "unload", permission = "pluginmanager.control.unload", minSize = 0)
    public void unload(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));
        if (plugin == null) {
            sender.sendMessage(_("control.unload.notfound", arguments.getString(0)));
            return;
        }

        if (plugin.getState() == PluginState.UNLOADED) {
            sender.sendMessage(_("control.unload.notloaded", arguments.getString(0)));
            return;
        }

        if (sender.isPlayer())
            backend.getLogger().info("[Command] \"" + sender.getName() + "\" unloads the plugin: " + plugin.getName());


        if (plugin.getState() == PluginState.ENABLED) {
            if (sender.hasPermission("pluginmanager.control.disable")) {
                plugin.disable();
            } else {
                sender.sendMessage(_("cmd.check.permission"));
                return;
            }
        }

        plugin.unload();
        sender.sendMessage(_("control.unload.success", plugin.getName()));
    }

    @Command(value = "reload", permission = "pluginmanager.control.reload", flags = "s", minSize = 1)
    public void reload(Backend backend, CommandExecutor<?> sender, ArgumentParser arguments) throws PluginManagerException {
        Plugin plugin = this.getPluginManager().getPlugin(arguments.getString(0));
        if (plugin == null) {
            sender.sendMessage(_("control.reload.notfound", arguments.getString(0)));
            return;
        }

        backend.getLogger().info("[Command] \"" + sender.getName() + "\" reloads the plugin: " + plugin.getName());

        if (plugin.getState() == PluginState.ENABLED) {
            plugin.disable();
        }

        if (!arguments.hasFlag('s') && sender.hasPermission("pluginmanager.control.unload"))
            plugin.unload();

        if (plugin.getState() == PluginState.UNLOADED)
            plugin.load();

        plugin.enable();
        sender.sendMessage(_("control.reload.success", plugin.getName()));
    }
}
