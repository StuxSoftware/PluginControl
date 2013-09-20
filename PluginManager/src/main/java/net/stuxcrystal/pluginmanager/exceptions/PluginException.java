package net.stuxcrystal.pluginmanager.exceptions;

/**
 * Base-exception for all exceptions of the plugin manager.
 */
public class PluginException extends FrontendException {

    private static final long serialVersionUID = 1L;

    public PluginException() {
    }

    public PluginException(String arg0) {
        super(arg0);
    }

    public PluginException(Throwable arg0) {
        super(arg0);
    }

    public PluginException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PluginException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

}
