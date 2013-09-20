package net.stuxcrystal.pluginmanager.io;

import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.configuration.PluginConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A cache for URLs.<p />
 *
 * Threadsafe.
 */
public class Cache {

    /**
     * Represents a cache item.
     */
    private class CacheItem {

        /**
         * The time where the item was added.
         */
        private final long addTime;

        /**
         * The file where the item is in.
         */
        private final File file;

        public CacheItem(File file, long addTime) {
            this.file = file;
            this.addTime = addTime;
        }

    }

    /**
     * The core-plugin.
     */
    private final PluginManager manager;

    private final Map<URL, CacheItem> cache = new HashMap<>();

    public Cache(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Downloads a file using the default user-agent.
     * @param url The URL to download.
     * @return The file where the content was downloaded.
     * @throws IOException If an I/O-Operation fails.
     */
    public File downloadFile(URL url) throws IOException {
        return downloadFile(url, null);
    }

    /**
     * Downloads a file using a given user-agent.
     * @param url       The URL to download.
     * @param userAgent The user-agent to use.
     * @return The file where the file was downloaded.
     * @throws IOException If an I/O-Operation fails.
     */
    public File downloadFile(URL url, String userAgent) throws IOException {
        PluginConfiguration.CacheConfiguration configuration = this.manager.getConfiguration().getCacheConfiguration();

        synchronized (cache) {
            if (cache.containsKey(url)) {
                CacheItem item = cache.get(url);
                if (System.currentTimeMillis() - item.addTime < configuration.maxCacheTime && item.file.exists()) {
                    return item.file;
                } else {
                    if (!item.file.exists()) {
                        this.manager.getLogger().warning("[Cache] Non-existing file in cache: " + item.file.getName());
                    }

                    cache.remove(url);
                }
            }
        }

        File file = File.createTempFile("cache_", "_plugincontrol");
        InputStream is = this.manager.getConnector().openConnection(url, userAgent).getInputStream();
        StreamUtilities.move(is, file);

        if (configuration.maxCacheTime > 0) {
            CacheItem item = new CacheItem(file, System.currentTimeMillis());

            synchronized (cache) {
                cache.put(url, item);
            }
        }

        return file;
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        this.manager.getLogger().info("[Cache] Clearing cache...");

        synchronized (cache) {
            for (CacheItem item : this.cache.values()) {
                item.file.delete();
            }
            this.cache.clear();
        }
    }

}
