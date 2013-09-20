package net.stuxcrystal.pluginmanager.archives;

import java.io.File;

/**
 * Represents an item in an archive.
 */
public class ArchiveItem {

    /**
     * Path to the directory.
     */
    private final String[] directory;

    /**
     * The name of the file.
     */
    private final String name;

    /**
     * The file where the data is stored.
     */
    private final File file;

    /**
     * Constructs an item.
     * @param directory The directory where the file should be stored.
     * @param name      The name of the directory.
     * @param file      The file where the data is stored.
     */
    public ArchiveItem(String[] directory, String name, File file) {
        this.directory = directory;
        this.name      = name;
        this.file      = file;
    }

    /**
     * A list of strings to the directory name.
     * @return The name of the directory.
     */
    public String[] getDirectory() {
        return this.directory;
    }

    /**
     * The name of the file. Don't use the filename provided in "File.getName()".
     * @return The name of the file.
     */
    public String getName() {
        return this.name;
    }

    /**
     * The file where the
     * @return
     */
    public File getFile() {
        return this.file;
    }

}
