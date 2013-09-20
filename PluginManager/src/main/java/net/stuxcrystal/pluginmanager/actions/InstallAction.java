package net.stuxcrystal.pluginmanager.actions;

import com.google.common.io.Files;
import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.archives.ArchiveItem;
import net.stuxcrystal.pluginmanager.archives.ArchiveManager;
import net.stuxcrystal.pluginmanager.configuration.PluginConfiguration;
import net.stuxcrystal.pluginmanager.exceptions.DependencyException;
import net.stuxcrystal.pluginmanager.exceptions.IOOperationException;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.repository.PluginInformation;
import net.stuxcrystal.pluginmanager.utils.ListUtils;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.stuxcrystal.pluginmanager.PluginManager._;

/**
 * Installs a plugin and its dependencies.<p />
 *
 * This is a extremely complex action because sometimes the plugins are
 * shipped in a archive.
 */
public class InstallAction {

    /**
     * Stores the information about the file.
     */
    private class PluginFile {

        /**
         * The original name of the file.
         */
        final String name;

        /**
         * The reference to the file.
         */
        final File file;

        /**
         * Sets the data of the PluginFile-Instance.
         *
         * @param name The name of the plugin.
         * @param file The file to the data.
         */
        public PluginFile(String name, File file) {
            this.name = name;
            this.file = file;
        }

    }

    /**
     * Loads the plugin synchronously with the server.
     */
    private class LoadTask implements Runnable {

        /**
         * The plugins to load.
         */
        final List<String> plugins;

        /**
         * The manager to use.
         */
        final PluginManager manager;

        /**
         * The Executor who triggered the installation.
         */
        final CommandExecutor executor;

        /**
         * Is the mode verbose.
         */
        final boolean verbose;

        /**
         * Constructs the LoadTast-Instance.
         *
         * @param manager The PluginManager instance.
         * @param plugins The plugins to load.
         */
        public LoadTask(PluginManager manager, CommandExecutor executor, List<String> plugins, boolean verbose) {
            this.plugins  = plugins;
            this.executor = executor;
            this.manager  = manager;
            this.verbose  = verbose;
        }

        @Override
        public void run() {
            Collections.reverse(this.plugins);

            executor.sendMessage(_("install.install.step.load"));

            for (String name : plugins) {
                if (verbose) executor.sendMessage(_("install.install.step.load.verbose", name));

                Plugin plugin = this.manager.getPlugin(name);
                if (plugin == null) {
                    executor.sendMessage(_("install.install.step.load.unknown", name));
                    continue;
                }

                try {
                    plugin.load();
                    plugin.enable();
                } catch (PluginManagerException e) {
                    executor.sendMessage(_("install.install.step.load.failed", name));
                }
            }
        }
    }

    /**
     * Reference to the plugin manager.
     */
    private final PluginManager manager;

    /**
     * Constructs the installer.
     * @param manager  The manager.
     */
    public InstallAction(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Installs a plugin and its dependencies.
     * @param name     The name of the plugin.
     * @param executor The executor who executes the plugin.
     * @param verbose  Should the output be verbose.
     * @param test     Should the plugin be installed virutally. (AKA: Dry-Test)
     * @throws PluginManagerException If an exception occured while installing the plugin.
     */
    public void install(String name, CommandExecutor executor, boolean verbose, boolean test, boolean load)
            throws PluginManagerException {

        PluginConfiguration.InstallConfiguration configuration = this.manager.getConfiguration().getInstallConfiguration();
        boolean cache = configuration.useCache;

        if (this.manager.getConfiguration().isVerbose()) verbose = true;

        executor.sendMessage(_("install.install.step.dependencies"));
        List<String> unsatisfied;
        try {
            unsatisfied = this.getUnsatisfiedDependencies(name, executor, verbose);
        } catch (IOException e) {
            throw new IOOperationException(e.getMessage(), e);
        }

        executor.sendMessage(_("install.install.step.download"));
        List<PluginFile> files;
        try {
            files = downloadPlugins(unsatisfied, executor, cache, verbose);
        } catch (IOException e) {
            throw new IOOperationException(e.getMessage(), e);
        }

        if (test) {
            executor.sendMessage(_("install.install.mode.dry"));
            return;
        }

        executor.sendMessage(_("install.install.step.install"));
        try {
            installPlugins(executor, files, configuration.useCache, verbose);
        } catch (IOException e) {
            throw new IOOperationException(e.getMessage(), e);
        }

        if (load) {
            this.manager.getPlatform().schedule(new LoadTask(
                    this.manager, executor, unsatisfied, verbose
            ));
        }
    }

    /**
     * Installs a bunch of plugins.
     * @param names
     * @param verbose
     * @throws IOException
     */
    private List<PluginFile> downloadPlugins(List<String> names, CommandExecutor executor, boolean cache, boolean verbose)
            throws IOException {
        List<PluginFile> result = new ArrayList<PluginFile>(names.size());

        for (int i = 0; i<names.size(); i++) {
            String name = names.get(i);

            PluginInformation information = this.manager.getRepositoryManager().getPluginInformation(name);

            if (verbose)
                executor.sendMessage(_("install.install.step.download.verbose", information.getName()));

            result.add(new PluginFile(information.getFilename(), this.download(information.getDownloadUrl(), cache)));

        }

        return result;
    }

    /**
     * Copies all downloaded files into the plugin directory.
     *
     * @param executor The executor who initiated the action.
     * @param files    The files to be copied.
     * @param verbose  Should there be a verbose output.
     * @throws IOException If an I/O-Operation fails.
     */
    private void installPlugins(CommandExecutor<?> executor, List<PluginFile> files, boolean cache, boolean verbose)
            throws IOException {
        ArchiveManager archiver = this.manager.getArchiver();
        File pluginDirectory = this.manager.getPluginDirectory();

        for (PluginFile file : files) {
            if (verbose) {
                executor.sendMessage(_("install.install.step.install.verbose", file.name));
            }

            if (archiver.isArchive(file.name)) {
                if (verbose) executor.sendMessage(_("install.install.step.install.unpack", file.file));

                ArchiveItem[] items = archiver.unpack(file.file, file.name);
                for (ArchiveItem item : items) {
                    File archiveFile = archiver.toFile(pluginDirectory, item);
                    StreamUtilities.copy(item.getFile(), archiveFile);
                    item.getFile().delete();
                }
            } else {
                StreamUtilities.copy(file.file, new File(pluginDirectory, file.name));
            }

            if (!cache) {
                file.file.delete();
            }
        }
    }

    /**
     * Installs a plugin.
     * @param name      The name of the plugin.
     * @param verbose   Should the output be verbose.
     * @throws IOException            if an I/O-Operation fails.
     * @throws PluginManagerException if a dependency couldn't be retrieved.
     */
    public List<String> getUnsatisfiedDependencies(String name, CommandExecutor executor, boolean verbose)
            throws IOException, PluginManagerException {

        PluginConfiguration.InstallConfiguration configuration = this.manager.getConfiguration().getInstallConfiguration();

        String realName = this.getRealName(name);
        if (realName == null) throw new DependencyException("Plugin not found.");

        Plugin[] plugins = this.manager.getPlugins();

        List<String> existing = new ArrayList<>(plugins.length);
        for (Plugin plugin : plugins) existing.add(plugin.getName());

        List<String> checked = new ArrayList<>();
        List<String> unchecked = new ArrayList<>(Arrays.asList(realName));

        List<String> newDependencies;
        while (true) {
            newDependencies = getDependencies(configuration, unchecked, checked, existing, verbose, executor);
            if (newDependencies == null)
                throw new DependencyException("A unsatisfied dependency was found.");

            checked.addAll(unchecked);
            unchecked.clear();
            unchecked.addAll(newDependencies);

            if (newDependencies.size() == 0)
                break;
        }

        checked.remove(name);
        return checked;
    }

    /**
     * Returns all unsatisfied dependencies of the given plugins.
     * @param configuration The configuration of the installer.
     * @param check         The plugins to check.
     * @param checked       The checked dependencies.
     * @param existing      Already installed plugins.
     * @param verbose       Should the action be verbose
     * @return All dependencies to satisfy.
     * @throws IOException If an I/O-Operation fails.
     */
    private List<String> getDependencies(PluginConfiguration.InstallConfiguration configuration,
                                         List<String> check, List<String> checked, List<String> existing,
                                         boolean verbose, CommandExecutor executor)
            throws IOException {

        List<String> newPlugins = new ArrayList<>();

        for (String item : check) {
            if (verbose)
                executor.sendMessage(_("install.install.step.dependencies.verbose", item));

            boolean cache = configuration.useCache;
            boolean headers = configuration.useHeaderDependencies;

            List<String> dependencies = getDependencies(item, cache, headers);
            if (dependencies == null) return null;

            for (String dependency : dependencies) {
                if (!ListUtils.contains(dependency, checked, newPlugins, existing, check))
                    newPlugins.add(dependency);
            }
        }

        return newPlugins;

    }

    /**
     * Returns the dependencies of the file.
     * @param name        The name of the plugin.
     * @param cache       Should the cache be used.
     * @param repoHeaders Should the repo-headers be used if dependencies are given.
     * @return A list of dependencies.
     * @throws IOException if an I/O-Operation fails.
     */
    private List<String> getDependencies(String name, boolean cache, boolean repoHeaders) throws IOException {

        PluginInformation info = this.manager.getRepositoryManager().getPluginInformation(name);

        if (info == null || info.getDownloadUrl() == null) return null;

        if (repoHeaders) {
            List<String> result = info.getDependencies();
            if (result != null) return result;
        }

        File file = this.download(info.getDownloadUrl(), cache);
        File _file = getPluginFile(file, info);
        if (_file == null) return null;

        Plugin plugin = this.manager.getPlatform().parseMeta(_file);
        if (plugin == null) return null;

        List<String> result = plugin.getDependencies(false);

        if (!cache) {
            file.delete();
            if (!file.equals(_file)) _file.delete();
        }

        return result;

    }

    /**
     * Downloads a file.
     * @param url   The url to download.
     * @param cache Should the download be cached.
     * @return A file containing the download.
     * @throws IOException If an I/O-Operation fails.
     */
    public File download(URL url, boolean cache) throws IOException {
        File file;

        if (cache) file = this.manager.getCache().downloadFile(url);
        else       {
            file = File.createTempFile("install_", "_plugincontrol");
            URLConnection connection = this.manager.getConnector().openConnection(url);
            if (connection == null) return null;
            InputStream is = connection.getInputStream();
            StreamUtilities.move(is, file);
        }

        return file;
    }

    /**
     * Returns the actual plugin file if the downloaded file is an archive.
     * @param file        The input file.
     * @param information The plugin information data.
     * @return The filename.
     */
    public File getPluginFile(File file, PluginInformation information) {
        if (file == null)
            return null;

        if (this.manager.getPlatform().isValidPlugin(information.getFilename()))
            return file;

        ArchiveManager archiver = this.manager.getArchiver();
        ArchiveItem result = null;

        if (archiver.isArchive(information.getFilename())) {
            ArchiveItem[] items = archiver.unpack(file, information.getFilename());

            for (ArchiveItem item : items) {

                if (item.getDirectory().length > 0) continue;
                if (!this.manager.getPlatform().isValidPlugin(item.getName())) continue;

                Plugin plugin = this.manager.getPlatform().parseMeta(item.getFile());
                if (plugin == null) continue;

                if (!plugin.getName().equalsIgnoreCase(information.getName())) continue;

                result = item;
                break;              // As soon as the item needed was found the loop should end.
            }

            for (ArchiveItem item : items) {
                if (item.equals(result)) continue;
                item.getFile().delete();
            }

            return result.getFile();
        } else {
            this.manager.getLogger().warning("[Install] Unsupported Archive: " + information.getFilename());
        }

        return null;
    }

    /**
     * Downloads the file.<p />
     *
     * Please note that the cache configuration does not affect the download method of the
     * repository.
     *
     * @param name  The name of the plugin.
     * @return The real name of the plugin or null.
     */
    private String getRealName(String name) {
        PluginInformation information = this.manager.getRepositoryManager().getPluginInformation(name);
        if (information == null) return null;
        return information.getName();
    }

}
