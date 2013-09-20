package net.stuxcrystal.pluginmanager.archives.types;

import net.stuxcrystal.pluginmanager.archives.ArchiveItem;
import net.stuxcrystal.pluginmanager.archives.Unpacker;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Unpacker for the default zip-archive.
 */
public class ZipArchive implements Unpacker {
    @Override
    public boolean isSupported(String name) {
        return name.endsWith(".zip");
    }

    @Override
    public ArchiveItem[] unpack(File file) throws IOException {
        List<ArchiveItem> items = new ArrayList<ArchiveItem>();
        ZipFile zf = new ZipFile(file);

        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if (entry.isDirectory()) continue;

            String rawName = entry.getName();

            int lastSlash = rawName.lastIndexOf('/');

            String targetFileName = rawName.substring(lastSlash+1);
            String[] targetDir = lastSlash==-1?new String[0]:rawName.substring(0, lastSlash).split("/");
            File tfile = File.createTempFile("ziparchive_", "_plugincontrol");

            StreamUtilities.move(zf.getInputStream(entry), tfile);

            items.add(new ArchiveItem(targetDir, targetFileName, tfile));
        }

        return items.toArray(new ArchiveItem[items.size()]);

    }
}
