package net.stuxcrystal.pluginmanager.repository;

import net.stuxcrystal.configuration.exceptions.ConfigurationException;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.utils.ReflectionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * This class manages all repositories.<p />
 *
 * This class is thread safe because repositories are used in other threads.
 */
public class RepositoryManager {

    /**
     * List of repositories.
     */
    private final List<Repository> repositories = new ArrayList<>();

    /**
     * Reference to the pluginmanager.
     */
    private final PluginManager manager;

    /**
     * Constructs the manager.
     * @param manager The instance of the core class.
     */
    public RepositoryManager(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Registers a repository if the repository is compatible.<p />
     *
     * While registering a repository the configuration of the repository will be loaded.
     *
     * @param repository The repository to add.
     * @return True if the repository was loaded successfully, false otherwise.
     */
    public boolean addRepository(Repository repository) {

        // Don't install a repository two times.
        synchronized(this) { if (repositories.contains(repository)) return false; };

        // Checks if the repository is allowed.
        if (!manager.getConfiguration().isAllowedRepository(repository)) {
            this.manager.getLogger().warning("[Repositories] Tried to register unknown repository: " + repository.getName());
            return false;
        }

        // Check if the repository is compatible.
        if (!repository.isCompatible(this.manager.getPlatform())) return false;

        // Load the configuration of the repository.
        if (!this.loadConfiguration(repository)) return false;

        // Register repository.
        synchronized(this) { repositories.add(repository); };

        return true;
    }

    /**
     * Unregisters a repository.
     * @param repository The repository to unregister.
     */
    public void removeRepository(Repository repository) {
        synchronized(this) { this.repositories.remove(repository); }
    }

    /**
     * Unregisters all repositories.
     */
    public void clearRepository() {
        synchronized(this) { this.repositories.clear(); }
    }

    /**
     * Reloads the repositories.
     */
    public void reload() {
        synchronized(this) {
            for (Repository repository : repositories) {
                this.loadConfiguration(repository);
            }
        }
    }

    public void updateConfiguration() {
        synchronized (this) {
            for (Repository repository : repositories) {
                Object section = repository.getSettings();
                if (section != null) {

                }
            }
        }
    }

    /**
     * Loads the configuration the repository.
     * @param repository The repository to load the configuration from.
     */
    public boolean loadConfiguration(Repository repository) {
        Class<?> cls = repository.getSettingsClass();
        if (cls != null) {
            File file = manager.getConfiguration().getRepositoryConfiguration(repository);
            Object configuration = null;

            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                configuration = ReflectionUtil.newInstance(cls);
                if (configuration == null) {
                    this.manager.getLogger().log(Level.SEVERE, "[Repositories] Failed to get default configuration.");
                    return false;
                }

            } else {
                try {
                    configuration = manager.getConfigurationLoader().parseFile(file, cls);
                } catch (ConfigurationException e) {
                    this.manager.getLogger().log(Level.SEVERE, "[Configuration] Failed to load configuration of: " + repository.getName(), e);
                    return false;
                }
            }

            try {
                this.manager.getConfigurationLoader().dumpFile(file, configuration);
            } catch (ConfigurationException e) {
                this.manager.getLogger().log(Level.WARNING, "[Configuration] Failed to update configuration of: " + repository.getName(), e);
            }

            repository.setSettings(configuration);
        }

        return true;
    }

    /**
     * Retrieves information about a plugin using all registered repositories.
     * @param name The name of the repository.
     * @return A plugin-information object if the plugin was found, null otherwise.
     */
    public PluginInformation getPluginInformation(String name) {

        PluginInformation information = null;

        List<Repository> repositories;
        synchronized(this) { repositories = Collections.unmodifiableList(this.repositories); }

        for (Repository repository : repositories) {
            information = repository.getPluginInformation(name);
            if (information != null) break;
        }


        return information;
    }

    /**
     * @return The count of the registered repositories.
     */
    public int getRepositoryCount() {
        synchronized(this) { return this.repositories.size(); }
    }

    /**
     * @return All registered repositories.
     */
    public List<Repository> getRepositories() {
        synchronized(this) { return new ArrayList<Repository>(this.repositories); }
    }

}
