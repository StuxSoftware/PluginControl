package net.stuxcrystal.pluginmanager.io;

import java.io.File;
import java.io.IOException;

/**
 * File Utilities
 */
public final class FileUtils {

    /**
     * Returns the canonical and absolute file.<br />
     * If the canonical path cannot be retrieved, this step will be ignored.
     *
     * @param file The file to query.
     * @return A file object.
     */
    public static File toAbsoluteCanonicalFile(File file) {

        try {
            file = file.getCanonicalFile();
        } catch (IOException ignored) {
        }

        return file.getAbsoluteFile();
    }

}
