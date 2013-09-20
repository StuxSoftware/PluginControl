package net.stuxcrystal.pluginmanager.exceptions.handlers;

import net.stuxcrystal.commandhandler.ArgumentParser;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.commandhandler.ExceptionHandler;
import net.stuxcrystal.commandhandler.utils.MessageColor;
import net.stuxcrystal.pluginmanager.PluginManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Handles exceptions thrown while performing a plugin operation.
 */
public abstract class PluginExceptionHandler<T extends Throwable> implements ExceptionHandler<T> {

    /**
     * Reference to the core class.
     */
    protected final PluginManager manager;

    /**
     * @param manager The core-class of the plugin
     */
    public PluginExceptionHandler(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Prints a stack trace.
     *
     * @param exception The exception thrown.
     * @param executor  The executor.
     * @param name      The name of the command.
     */
    protected final void printStackTrace(Throwable exception, CommandExecutor<?> executor, String name) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(baos));

        if (executor.hasPermission("pluginmanager.admin.exception.show")) {
            boolean printToChat = executor.hasPermission("pluginmanager.admin.exception.verbose") && executor.isPlayer();
            for (String line : baos.toString().split("\n")) {
                if (printToChat) executor.sendMessage("" + MessageColor.RED + MessageColor.ITALIC + line);
                this.manager.getLogger().severe("[Command:" + name + "] " + line);
            }

            if (!printToChat) {
                executor.sendMessage("" + MessageColor.RED + MessageColor.ITALIC + exception.getLocalizedMessage());
            }
        }
    }
}
