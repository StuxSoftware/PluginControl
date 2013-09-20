package net.stuxcrystal.pluginmanager.exceptions.handlers;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;

import java.io.IOException;

import static net.stuxcrystal.pluginmanager.PluginManager._;

/**
 * Handles a PluginException.
 */
public class IOExceptionHandler extends PluginExceptionHandler<IOException> {
    /**
     * @param manager The core-class of the plugin
     */
    public IOExceptionHandler(PluginManager manager) {
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
    public void exception(IOException exception, String name, CommandExecutor<?> executor, ArgumentParser arguments) {
        executor.sendMessage(_("exception.io.fail"));
        this.printStackTrace(exception, executor, name);
    }
}
