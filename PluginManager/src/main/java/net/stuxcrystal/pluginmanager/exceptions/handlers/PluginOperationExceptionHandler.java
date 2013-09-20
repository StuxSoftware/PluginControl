package net.stuxcrystal.pluginmanager.exceptions.handlers;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;

import static net.stuxcrystal.pluginmanager.PluginManager._;

/**
 * Handles a PluginException.
 */
public class PluginOperationExceptionHandler extends PluginExceptionHandler<PluginManagerException> {
    /**
     * @param manager The core-class of the plugin
     */
    public PluginOperationExceptionHandler(PluginManager manager) {
        super(manager);
    }

    /**
     * Handles a plugin exception.
     * @param exception The exception that was thrown.
     * @param name      The name of the command.
     * @param executor  The sender who executed the command.
     * @param arguments The arguments that were passed to the command.
     */
    @Override
    public void exception(PluginManagerException exception, String name, CommandExecutor<?> executor, ArgumentParser arguments) {
        executor.sendMessage(_("exception.operation.fail"));
        this.printStackTrace(exception, executor, name);
    }
}
