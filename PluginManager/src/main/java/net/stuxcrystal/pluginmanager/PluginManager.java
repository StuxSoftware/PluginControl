package net.stuxcrystal.pluginmanager;

import net.stuxcrystal.commandhandler.CommandHandler;
import net.stuxcrystal.configuration.ConfigurationLoader;
import net.stuxcrystal.configuration.exceptions.ConfigurationException;
import net.stuxcrystal.configuration.generators.yaml.YamlGenerator;
import net.stuxcrystal.pluginmanager.archives.ArchiveManager;
import net.stuxcrystal.pluginmanager.commands.GeneralCommands;
import net.stuxcrystal.pluginmanager.commands.InstallCommands;
import net.stuxcrystal.pluginmanager.commands.PluginControl;
import net.stuxcrystal.pluginmanager.commands.PluginInformation;
import net.stuxcrystal.pluginmanager.configuration.Localization;
import net.stuxcrystal.pluginmanager.configuration.PluginConfiguration;
import net.stuxcrystal.pluginmanager.configuration.PluginVersion;
import net.stuxcrystal.pluginmanager.exceptions.PluginManagerException;
import net.stuxcrystal.pluginmanager.exceptions.handlers.IOExceptionHandler;
import net.stuxcrystal.pluginmanager.exceptions.handlers.PluginOperationExceptionHandler;
import net.stuxcrystal.pluginmanager.io.Cache;
import net.stuxcrystal.pluginmanager.io.Connector;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.metrics.MCStatsImplementation;
import net.stuxcrystal.pluginmanager.metrics.PluginMetricsImplementation;
import net.stuxcrystal.pluginmanager.repository.RepositoryManager;
import net.stuxcrystal.pluginmanager.repository.packaged.BukGet.BukGetRepository;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The platform independent plugin manager.
 */
public final class PluginManager {

    /**
     * The instance of the plugin manager.
     */
    private static PluginManager INSTANCE;

    /**
     * The loader for configurations.
     */
    private static final ConfigurationLoader CONFIGURATION_LOADER = new ConfigurationLoader();

    /**
     * Contains the version of the plugin.
     */
    private PluginVersion version;

    /**
     * The platform this plugin is running on.
     */
    final ServerPlatform platform;

    /**
     * The localization of the plugin.
     */
    final Localization localization;

    /**
     * Configuration of this plugin.
     */
    private PluginConfiguration configuration;

    /**
     * Loads the user interface.
     */
    private final UserInterface ui = new UserInterface(this);

    /**
     * Lists all available actions.
     */
    private final ActionProvider actions;

    /**
     * Manager for repositories.
     */
    private final RepositoryManager repositoryManager;

    /**
     * The connector of the plugin.
     */
    private final Connector connector;

    /**
     * The cache to speed up repetitive downloads.
     */
    private final Cache cache;

    /**
     * Manages archives.
     */
    private final ArchiveManager archiver;

    /**
     * The implementation of metrics.
     */
    private final PluginMetricsImplementation metrics;

    /**
     * Initiates the PluginManager
     * @param platform The server platform the plugin is running on.
     */
    public PluginManager(ServerPlatform platform) {
        this.platform = platform;
        this.localization = new Localization(this, null);

        if (INSTANCE != null)
            throw new IllegalStateException("The plugin manager is already loaded.");

        INSTANCE = this;

        this.actions = new ActionProvider(this);
        this.repositoryManager = new RepositoryManager(this);
        this.connector = new Connector(this);
        this.cache = new Cache(this);
        this.archiver = new ArchiveManager();

        this.metrics = new MCStatsImplementation();
    }

    /**
     * Called when the plugin is be loaded.<p />
     * <p/>
     * A plugin should be loaded one time.
     */
    public void load() {
        try {
            this.version = CONFIGURATION_LOADER.parseStream(
                    this.getPlatform().getResource("version.yml"),
                    new YamlGenerator(),
                    PluginVersion.class
            );
        } catch (ConfigurationException | IOException e) {
            this.getLogger().log(Level.SEVERE, "[Load] Failed to parse meta-data.", e);
            this.version = new PluginVersion();
        }
        this.getLogger().info("[Load] Meta-Data parsed...");

        CommandHandler handler = this.platform.getCommands();

        // Register exception handlers.
        handler.registerExceptionHandler(IOException.class, new IOExceptionHandler(this));
        handler.registerExceptionHandler(PluginManagerException.class, new PluginOperationExceptionHandler(this));

        // Register the localization system.
        handler.getTranslationManager().setTranslationHandler(localization);

        // Register an exception
        handler.registerCommands(new GeneralCommands(this));    // General commands
        handler.registerCommands(new PluginControl(this));      // Commands to control plugins.
        handler.registerCommands(new PluginInformation(this));  // Information about plugins.
        handler.registerCommands(new InstallCommands(this));    // Installation of plugins.

        this.getLogger().info("[Load] Commands initialized...");
    }

    /**
     * Called if the server enables this plugin.
     */
    public void enable() {
        if (this.loadConfig())
            this.getLogger().info("[Enable] Configuration read...");
        else
            this.getLogger().warning("[Enable] Failed to load configuration properly.");

        this.repositoryManager.addRepository(new BukGetRepository());

        if (this.repositoryManager.getRepositoryCount() == 0) {
            this.getLogger().warning("[Repositories] No repositories registered.");
        }

        this.getLogger().info("[Enable] Default Repositories registered...");

        if (this.getConfiguration().getMetricsConfiguration().usePluginMetrics) {
            metrics.initialize(this);
            metrics.start();
            this.getLogger().info("[Enable] Metrics enabled");
        } else {
            this.getLogger().info("[Enable] Metrics disabled by configuration...");
        }


        this.getLogger().info("[Enable] Enabled PluginControl on " + this.getPlatformName());
    }

    /**
     * Called when this plugin is disabled.
     */
    public void disable() {
        if (this.getConfiguration().getMetricsConfiguration().usePluginMetrics) metrics.stop();
        this.getCache().clear();
        this.getLogger().info("[Disable] PluginControl disabled...");
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        // Disable metrics if enabled.
        if (this.getConfiguration().getMetricsConfiguration().usePluginMetrics) {
            this.metrics.stop();
        }

        // Reloads the configuration.
        if (this.loadConfig())
            this.getLogger().info("[Reload] Configuration read...");
        else
            this.getLogger().warning("[Reload] Failed to load configuration properly.");

        this.repositoryManager.reload();

        // Reload the subsystems...
        this.cache.clear();

        // Re-enable metrics if opted in.
        if (this.getConfiguration().getMetricsConfiguration().usePluginMetrics) {
            this.metrics.initialize(this);
            this.metrics.start();
        }

        // Reloads the server.
        this.platform.reload();
    }

    /**
     * Resets the plugin.
     */
    public void reset() {
        this.configuration = new PluginConfiguration();

        // Copy the localization
        this.getPlatform().copyResource("localization.lang", "lang.conf");

        // Create the default configuration.
        try {
            CONFIGURATION_LOADER.dumpFile(
                    new File(this.getPlatform().getConfigurationDirectory(), "config.yml"),
                    this.configuration
            );
        } catch (ConfigurationException e) {
            this.getLogger().log(Level.WARNING, "[Configuration] Failed to save configuration...", e);
        }

        this.loadDefaultLocalization();

        this.platform.reload();
    }

    /**
     * @return The name of the platform.
     */
    public String getPlatformName() {
        return this.platform.getPlatformName();
    }

    /**
     * @return The logger for this plugin.
     */
    public Logger getLogger() {
        return platform.getLogger();
    }

    /**
     * @return The plugin directory.
     */
    public File getPluginDirectory() {
        return platform.getPluginDirectory();
    }

    /**
     * @return All installed plugins.
     */
    public Plugin[] getPlugins() {
        return platform.getPlugins();
    }

    /**
     * Searches and returns a plugin.
     *
     * @param name The plugin with the given name.
     * @return A plugin wrapper..
     */
    public Plugin getPlugin(String name) {
        return platform.getPlugin(name);
    }

    /**
     * Returns the instance of the server platform.
     *
     * @return A server platform object representing the current platform.
     */
    public ServerPlatform getPlatform() {
        return platform;
    }

    /**
     * @return the current configuration.
     */
    public PluginConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @return The class handling the user interface.
     */
    public UserInterface getUserInterface() {
        return this.ui;
    }

    /**
     * @return A class containing the version of the plugin.
     */
    public PluginVersion getVersion() {
        return this.version;
    };

    /**
     * @return Provides the actions.
     */
    public ActionProvider getActions() {
        return this.actions;
    }

    /**
     * @return The current configuration loader.
     */
    public ConfigurationLoader getConfigurationLoader() {
        return CONFIGURATION_LOADER;
    }

    /**
     * @return The RepositoryManager of the plugin.
     */
    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    /**
     * @return The connector that connects to a server.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * @return The cache to speed up repetitive downloads
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * @return The unpacker of the plugin.
     */
    public ArchiveManager getArchiver() {
        return archiver;
    }

    /**
     * Translates the key into a format.
     *
     * @param key    The key to the message.
     * @param format The strings for formatting
     * @return The translated string.
     */
    public String translate(String key, Object... format) {
        return this.localization.translate(key, format);
    }

    /**
     * Loads the configuration.
     *
     * @return False if the configuration couldn't be loaded.
     */
    private boolean loadConfig() {
        // Basic configuration file.
        File configurationDirectory = this.getPlatform().getConfigurationDirectory();
        File configurationFile = new File(configurationDirectory, "config.yml");

        boolean fallback = false;

        // Create configuration file.
        if (!configurationFile.exists()) {
            if (!configurationDirectory.exists()) {
                configurationDirectory.mkdirs();
            }

            try {
                CONFIGURATION_LOADER.dumpFile(configurationFile, new PluginConfiguration());
            } catch (ConfigurationException e) {
                this.getLogger().log(Level.WARNING, "[Configuration] Failed to write default configuration...", e);
                fallback = true;
            }
        }

        // Load configuration.
        if (!fallback) {
            // Try to load the configuration.
            try {
                this.configuration = CONFIGURATION_LOADER.parseFile(configurationFile, PluginConfiguration.class);
            } catch (ConfigurationException e) {
                this.getLogger().log(Level.WARNING, "[Configuration] Failed to parse configuration file... Falling back to default...");
                this.configuration = new PluginConfiguration();
                fallback = true;
            }
        } else {
            loadDefaultLocalization();
            return false;
        }

        // Store updated configuration to prevent getting exceptions.
        if (!fallback) {
            try {
                CONFIGURATION_LOADER.dumpFile(configurationFile, this.configuration);
            } catch (ConfigurationException e) {
                // A fail will not affect the configuration proceed normally.
                this.getLogger().log(Level.WARNING, "[Configuration] Failed to store an updated version of the config.", e);
            }
        }

        // Loads the configuration.
        if (!fallback) {

            // Create default configuration.
            File langFile = this.configuration.getLocalizationFile();
            if (!langFile.exists()) {
                InputStream is = this.platform.getResource("localization.lang");

                if (is == null) {
                    this.getLogger().warning("[Localization] Failed to obtain stream to default localization...");
                    return false;
                }

                try {
                    StreamUtilities.move(is, langFile);
                } catch (IOException e) {
                    this.getLogger().log(Level.WARNING, "[Localization] Failed to copy language file... Falling back to default.", e);
                    loadDefaultLocalization();
                    return false;
                }
            }

            this.localization.update(langFile);
        } else {
            this.getLogger().log(Level.WARNING, "[Localization] Using default localization becuase configuration couldn't be loaded.");
            loadDefaultLocalization();
            return false;
        }

        return true;
    }

    /**
     * Loads the default configuration.
     */
    private void loadDefaultLocalization() {
        InputStream is = this.platform.getResource("localization.lang");

        if (is == null) {
            this.getLogger().warning("[Localization] Failed to obtain stream to default localization...");
        }


        BufferedReader br;
        this.localization.update((br = new BufferedReader(new InputStreamReader(is))));
        try {
            br.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * @return The PluginManager-Instance.
     */
    public static PluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * Translates a string.
     * @param key    The translation key.
     * @param values The values passed to the translation.
     * @return The translated string.
     */
    public static String _(String key, Object... values) {
        return getInstance().translate(key, values);
    }
}
