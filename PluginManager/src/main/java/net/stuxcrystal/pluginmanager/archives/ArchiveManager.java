package net.stuxcrystal.pluginmanager.archives;

import net.stuxcrystal.pluginmanager.archives.types.ZipArchive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages unpacker.<p />
 *
 * This class is thread safe because this method is usually called from another
 * thread.
 */
public class ArchiveManager {

    private final List<Unpacker> unpackers = new ArrayList<Unpacker>(Arrays.asList(
            new ZipArchive()
    ));

    /**
     * Registers an archive.
     * @param unpacker The unpacker to add.
     */
    public void registerUnpacker(Unpacker unpacker) {
        synchronized (this) {
            if (!this.unpackers.contains(unpacker)) this.unpackers.add(unpacker);
        }
    }

    /**
     * Removes an unpacker.
     * @param unpacker The unpacker to remove.
     */
    public void unregisterUnpacker(Unpacker unpacker) {
        synchronized (this) {
            this.unpackers.remove(unpacker);
        }
    }

    /**
     * Returns a list of unpackers.
     * @return
     */
    public List<Unpacker> getUnpackers() {
        synchronized (this) {
            return new ArrayList<Unpacker>(this.unpackers);
        }
    }

    /**
     * Reprensents an archive.
     * @param file     The file to unpack.
     * @param filename The original filename.
     * @return A list of files inside the archive or null if the archive is not supported.
     */
    public ArchiveItem[] unpack(File file, String filename) {
        Unpacker unpacker = null;
        synchronized (this) {
            for (Unpacker item : this.getUnpackers()) {
                if (item.isSupported(filename)) {
                    unpacker = item;
                    break;
                }
            }
        }

        if (unpacker == null)
            return null;

        try {
            return unpacker.unpack(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if the given filename is usually supported by this
     * archiver.
     * @param filename The filename to check.
     * @return true if the file is supported by this manager.
     */
    public boolean isArchive(String filename) {
        for (Unpacker unpacker : this.getUnpackers()) {
            if (unpacker.isSupported(filename)) return true;
        }
        return false;
    }

    /**
     * Generates the file.
     * @param basedir The basic directory.
     * @param item    The item that generates the path.
     * @return A file object.
     */
    public File toFile(File basedir, ArchiveItem item) {
        File current = basedir;
        for (String dirname : item.getDirectory()) {
            current = new File(current, dirname);
        }
        return new File(current, item.getName());
    }

}
