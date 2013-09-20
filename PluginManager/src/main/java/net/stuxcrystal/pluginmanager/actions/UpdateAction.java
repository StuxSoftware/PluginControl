package net.stuxcrystal.pluginmanager.actions;

import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.PluginState;
import net.stuxcrystal.pluginmanager.archives.ArchiveItem;
import net.stuxcrystal.pluginmanager.archives.ArchiveManager;
import net.stuxcrystal.pluginmanager.exceptions.DependencyException;
import net.stuxcrystal.pluginmanager.exceptions.IOOperationException;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;
import net.stuxcrystal.pluginmanager.io.Connector;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.repository.PluginInformation;

import static net.stuxcrystal.pluginmanager.PluginManager._;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Updates the plugin.
 */
public class UpdateAction {

    /**
     * Reference to the core class.
     */
    private final PluginManager manager;

    /**
     * Constructs the action class.
     *
     * @param manager The manager-class.
     */
    public UpdateAction(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Triggers the update routine of the manager.
     *
     * @param executor The executor who triggered the update.
     * @param plugins  The plugins to update.
     * @param verbose  Should the output be verbose.
     * @throws PluginManagerException If the action fails.
     */
    public void update(CommandExecutor<?> executor, Plugin[] plugins, boolean verbose)
            throws PluginManagerException {

        boolean useCache = this.manager.getConfiguration().getInstallConfiguration().useCache;
        if (this.manager.getConfiguration().isVerbose()) verbose = true;

        // Disable plugins...
        executor.sendMessage(_("install.update.step.disable"));
        for (Plugin plugin : plugins) {
            disablePlugin(executor, plugin, verbose);
        }

        // Update plugin files.
        executor.sendMessage(_("install.update.step.download"));
        Map<Plugin, File> pluginFiles = new HashMap<Plugin, File>();
        for (Plugin plugin : plugins) {
            try {
                pluginFiles.put(plugin, downloadUpdates(executor, plugin, useCache, verbose));
            } catch (IOException e) {
                throw new IOOperationException("Failed to download plugin: " + plugin.getName(), e);
            }
        }

        // Replace plugins
        executor.sendMessage(_("install.update.step.replace"));
        for (Map.Entry<Plugin, File> update : pluginFiles.entrySet()) {
            try {
                this.replacePlugin(executor, update.getKey(), update.getValue(), verbose);
            } catch (IOException e) {
                executor.sendMessage(_("install.update.step.replace.fail", update.getKey().getName()));
            }
        }

        executor.sendMessage(_("install.update.step.load"));
        for (Plugin plugin : plugins) {
            this.enablePlugin(executor, plugin, verbose);
        }

    }

    /**
     * Disables a plugin before the update will be peformed.<p />
     *
     * The plugin has to be disabled to avoid exception caused by the update.
     *
     * @param executor The player who triggered the update.
     * @param plugin   The plugin to disable.
     * @param verbose  Should the output be verbose?
     * @throws PluginManagerException If the action fails.
     */
    private void disablePlugin(CommandExecutor executor, Plugin plugin, boolean verbose)
            throws PluginManagerException {

        if (verbose) executor.sendMessage(_("install.update.step.disable.verbose", plugin.getName()));

        if (plugin.getState() != PluginState.UNLOADED) {

            // Disable the plugin if needed.
            if (plugin.getState() == PluginState.ENABLED) {
                plugin.disable();
            }

            // Unload the plugin.
            plugin.unload();
        }
    }

    /**
     * Updates the plugin.
     *
     * @param executor The player that triggered the update.
     * @param plugin   The plugin where the updates should be downloaded.
     * @param useCache Should the cache be used?
     * @param verbose  Should the output be verbose?
     * @return A file-object denoting the Plugin jar-File or null if the download provided is invalid.
     * @throws IOException If an I/O-Operation fails.
     */
    private File downloadUpdates(CommandExecutor executor, Plugin plugin, boolean useCache, boolean verbose)
            throws IOException {

        if (verbose) executor.sendMessage(_("install.update.step.download.verbose", plugin.getName()));

        // Query information about the plugin.
        PluginInformation information = this.manager.getRepositoryManager().getPluginInformation(plugin.getName());

        // Download the raw file.
        File rawUpdate = download(information.getDownloadUrl(), useCache);

        // Extract the plugin-jar if necessary.
        File updateFile = getRealPluginFile(rawUpdate, information.getFilename(), plugin);

        // Delete the raw file if the file is not a cache file and is not the file to update.
        if (rawUpdate.equals(updateFile) && !useCache) {
            rawUpdate.delete();
        }

        return updateFile;
    }

    /**
     * Replaces the original plugin with its update.
     *
     * @param executor The player who triggered the update.
     * @param plugin   The original plugin.
     * @param update   The file to the update.
     * @param verbose  Should the action be verbose?
     * @throws IOException If an I/O-Operation fails.
     */
    private void replacePlugin(CommandExecutor executor, Plugin plugin, File update, boolean verbose)
            throws IOException {
        if (verbose) executor.sendMessage(_("install.update.step.replace.verbose", plugin.getName()));

        File pluginFile = plugin.getFile();
        StreamUtilities.copy(update, pluginFile);
    }

    /**
     * Enables a plugin.
     *
     * @param executor The player who triggered the update.
     * @param plugin   The plugin to enable.
     * @param verbose  Should the action be verbose.
     * @throws PluginManagerException If the action fails.
     */
    public void enablePlugin(CommandExecutor executor, Plugin plugin, boolean verbose) throws PluginManagerException {
        if (verbose) executor.sendMessage(_("install.update.step.load.verbose", plugin.getName()));
        plugin.load();
        plugin.enable();
    }

    /**
     * Returns the plugin file.
     *
     * @param file     The file to test (or extract from)
     * @param name     The name of the file.
     * @param original The original plugin.
     * @return A file object pointing to the plugin.
     * @throws IOException If an I/O-Operation fails.
     */
    private File getRealPluginFile(File file, String name, Plugin original) throws IOException {

        // Check if the file already is a valid plugin file.
        if (this.manager.getPlatform().isValidPlugin(name))
            return file;

        ArchiveManager manager = this.manager.getArchiver();

        if (manager.isArchive(name)) {
            ArchiveItem[] items = manager.unpack(file, name);

            File result = null;

            for (ArchiveItem item : items) {
                if (result != null && item.getDirectory().length == 0) {
                    Plugin internal = this.manager.getPlatform().parseMeta(file);
                    if (internal != null && internal.getName().equals(original.getName())) {
                        result = item.getFile();
                        continue;
                    }
                }

                item.getFile().delete();
            }

            if (result != null) return result;
        }

        throw new IOException("Unsupported archive: "+ name);
    }

    /**
     * Downloads the file.
     * @param url      The URL to download.
     * @param useCache Should the cache be used.
     * @return The file object where the file was downloaded.
     */
    private File download(URL url, boolean useCache) throws IOException {

        File download = null;

        if (!useCache) {
            download = File.createTempFile("update_", "_plugincontrol");

            Connector connector = this.manager.getConnector();
            URLConnection connection = connector.openConnection(url);
            StreamUtilities.move(connection.getInputStream(), download);
        } else {
            download = this.manager.getCache().downloadFile(url);
        }

        return download;
    }

}
