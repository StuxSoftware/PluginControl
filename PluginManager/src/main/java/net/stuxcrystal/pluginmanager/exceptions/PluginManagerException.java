package net.stuxcrystal.pluginmanager.exceptions;

/**
 * Base-exception for all exceptions of the plugin manager.
 */
public class PluginManagerException extends Exception {

    private static final long serialVersionUID = 1L;

    PluginManagerException() {
    }

    PluginManagerException(String arg0) {
        super(arg0);
    }

    PluginManagerException(Throwable arg0) {
        super(arg0);
    }

    PluginManagerException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    PluginManagerException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

}
