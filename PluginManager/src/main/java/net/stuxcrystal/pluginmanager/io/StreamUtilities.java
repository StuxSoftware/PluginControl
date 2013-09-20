package net.stuxcrystal.pluginmanager.io;

import java.io.*;

/**
 * Utilities for Streams.
 */
public final class StreamUtilities {

    /**
     * Moves a file.
     *
     * @param src The source file.
     * @param dst The destination file.
     * @throws IOException If an I/O-Operation fails.
     */
    public static void copy(File src, File dst) throws IOException {
        createFile(dst);

        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dst);

            move(fis, fos);
        } finally {
            if (fis != null) try {fis.close(); } catch (IOException ignored) {};
            if (fos != null) try {fos.close(); } catch (IOException ignored) {};
        }
    }

    /**
     * Moves a stream.
     *
     * @param input  The source stream.
     * @param output The destination stream.
     * @throws IOException thrown when an I/O-Operation fails.
     */
    public static void move(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
    }

    /**
     * Writes all data to the given file.
     *
     * @param stream Source Stream.
     * @param file   Destination file.
     * @throws IOException thrown when an I/O-Operation fails.
     */
    public static void move(InputStream stream, File file) throws IOException {
        createFile(file);
        FileOutputStream fos = new FileOutputStream(file);
        move(stream, fos);
        fos.close();
    }

    /**
     * Creates the file and its parent directory if needed.
     *
     * @param file The file to create.
     * @throws IOException If an I/O-Operation fails.
     */
    private static void createFile(File file) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs())
                    throw new IOException("Failed to create directory.");
            }

            file.createNewFile();
        }
    }

    /**
     * Converts a stream to a string.
     * @param stream The source of the data.
     * @return The converted string.
     */
    public static String toString(InputStream stream) {
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        try { stream.close(); } catch (IOException ignored) {}
        return result;
    }

    /**
     * Converts a stream to a string.
     * @param stream   The source of the data.
     * @param encoding The encoding of the data.
     * @return The converted string.
     */
    public static String toString(InputStream stream, String encoding) {
        java.util.Scanner s = new java.util.Scanner(stream, encoding).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        try { stream.close(); } catch (IOException ignored) {}
        return result;
    }

}
