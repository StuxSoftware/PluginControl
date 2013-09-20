package net.stuxcrystal.pluginmanager.exceptions;

/**
 * Base-exception for all exceptions of the plugin manager.
 */
public class NotSupportedException extends FrontendException {

    private static final long serialVersionUID = 1L;

    public NotSupportedException() {
    }

    public NotSupportedException(String arg0) {
        super(arg0);
    }

    public NotSupportedException(Throwable arg0) {
        super(arg0);
    }

    public NotSupportedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public NotSupportedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

}
