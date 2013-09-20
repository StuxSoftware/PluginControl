package net.stuxcrystal.pluginmanager.archives;

import java.io.File;
import java.io.IOException;

/**
 * A class implementing this interface will support
 * a archive type.
 */
public interface Unpacker {

    /**
     * Checks if the specified filename marks the supported
     * archive type.
     * @param name The filename.
     * @return true if the filename is an archive.
     */
    public boolean isSupported(String name);

    /**
     * Unpacks an item.
     * @return The item to unpack.
     * @throws IOException if an I/O-Operation fails.
     */
    public ArchiveItem[] unpack(File file) throws IOException;

}
