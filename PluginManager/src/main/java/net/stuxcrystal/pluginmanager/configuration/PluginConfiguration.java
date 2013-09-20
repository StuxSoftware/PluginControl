package net.stuxcrystal.pluginmanager.configuration;

import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.configuration.annotations.Configuration;
import net.stuxcrystal.configuration.annotations.Value;
import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.io.FileUtils;
import net.stuxcrystal.pluginmanager.repository.Repository;

import java.io.File;
import java.util.*;

/**
 * PluginConfiguration for the plugin.
 */
@Configuration(header={
        "This is the configuration of PluginControl.",
        "Please make sure that PluginControl stays usuable.",
        ""
})
public class PluginConfiguration {

    @Configuration(header={""})
    public static class UserInterface {

        public static enum PaginationShowPolicy {
            NEVER, NEEDED, ALWAYS;
        }

        @Value(name = "max_lines", comment = {
                "How many lines can a single page contain?",
                "If the value is -1, nothing will be pagized."
        })
        public int pagination_lines = 19;

        @Value(name = "forcePageNumbers", comment = {
                "",
                "When should page numbers should be shown?",
                "Values:",
                "  NEVER:  Never show page numbers. Even if its paginated.",
                "  NEEDED: Only show page numbers when it is needed.",
                "  ALWAYS: Always show page numbers."
        })
        public PaginationShowPolicy forcePageNumbers = PaginationShowPolicy.NEEDED;

        /**
         * Used for instantiating the object.
         */
        public UserInterface() {
        }

        /**
         * Used for the default UI-Settings.
         *
         * @param lines            Maximal amount of lines shown.
         * @param forcePageNumbers Should the output be forcePageNumbers.
         */
        public UserInterface(int lines, PaginationShowPolicy forcePageNumbers) {
            this.pagination_lines = lines;
            this.forcePageNumbers = forcePageNumbers;
        }
    }

    @Configuration
    public static class RepositoryConfiguration {

        @Value(name="allow-unknown", comment= {
                "Allow unknown repositories."
        })
        public boolean allowUnknown = false;

        @Value(name="repositories", comment= {
                "",
                "List of all activated repositories. (Case-Sensitive)",
                "Key:   Name of the repository.",
                "Value: File of it's configuration."
        })
        private Map<String, String> repositories = defaultConfiguration();

        private Map<String, String> defaultConfiguration() {
            Map<String, String> repos = new LinkedHashMap<>();

            repos.put("DevBukkit", "devbukkit.yml");

            return repos;
        }

    }

    @Configuration
    public static class ProxyConfiguration {

        public static enum ProxyType {
            SYSTEM, NONE, SOCKS, HTTP
        }

        @Value(name="type", comment={
                "The type of the proxy.",
                "Values:",
                "  SYSTEM:   Use the system proxy. (Default)",
                "  NONE:     Don't use a proxy.",
                "  SOCKS:    The specified proxy is a Socks4 Proxy.",
                "  HTTP:     The specified proxy is a HTTP-Proxy.",
                " ",
                "Notes on MineShafter: Set the proxy to NONE to enable web functionality."
        })
        public ProxyType type = ProxyType.SYSTEM;

        @Value(name="host")
        public String host = "";

        @Value(name="port")
        public int port = 0;
    }

    @Configuration
    public static class CacheConfiguration {

        @Value(name="max-time", comment={
                "The time how long a cache item should be stored. (in milliseconds)",
                "Set this value to -1 to disable the cache."
        })
        public long maxCacheTime = 24*60*60*1000;
    }

    @Configuration
    public static class InstallConfiguration {

        @Value(name="use-header-dependencies", comment={
                "Should the dependencies shown in the header be used?"
        })
        public boolean useHeaderDependencies = false;

        @Value(name="use-cache", comment={
                "",
                "Allow the installer to use the cache.",
                "If disabled the install will download the package multiple times if",
                "'use-header-dependencies' is deactivated."
        })
        public boolean useCache = true;

    }

    @Configuration
    public static class MetricsConfiguration {

        @Value(name="use-metrics", comment = {
                "Should plugin-metrics be disabled for this plugin. (opt-out remains active)"
        })
        public boolean usePluginMetrics = true;

        @Value(name="config-file", comment = {
                "",
                "Where should the configuration of plugin-metrics be stored?",
                "Values:",
                "  server:confdir    The configuration directory of the server.",
                "  server:basedir    The home directory of the server.",
                "  server:plugindir  The directory where the plugins are stored.",
                "  plugin:confdir    The configuration directory of the plugin.",
        })
        private String metricsPath = "${server:confdir}/PluginMetrics/config.yml";

        /**
         * Returns the configured metrics directory.
         * @return The metrics directory.
         */
        public File getMetricsFile() {
            Map<String, String> format = new HashMap<>();
            format.put("server:confdir", PluginManager.getInstance().getPlatform().getConfigurationDirectory().getParent());
            format.put("server:plugindir", PluginManager.getInstance().getPlatform().getPluginDirectory().toString());
            format.put("server:basedir", FileUtils.toAbsoluteCanonicalFile(new File(".")).toString());
            format.put("plugin:confdir", PluginManager.getInstance().getPlatform().getConfigurationDirectory().toString());

            return new File(FormatUtils.format(metricsPath, format));
        }

    }

    @Value(name = "lang", comment = {
            "Settings for the language.",
            "The file is located in the default plugin directory."
    })
    private String localization = "lang.conf";

    @Value(name = "ui", comment = {
            "",
            "Settings for the user interface.",
            "Special users:",
            "  console: Settings for the console.",
            "  default: Everything that is not listed here."
    })
    private Map<String, UserInterface> uisettings = getDefaultUISettings();

    @Value(name = "repositories", comment = {
            "",
            "Configuration for repositories."
    })
    private RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();

    @Value(name = "proxy", comment= {
            "",
            "Proxy settings"
    })
    private ProxyConfiguration proxy = new ProxyConfiguration();

    @Value(name = "user-agent", comment= {
            "",
            "The user-agent transmitted while connecting to a web-server."
    })
    private String useragent = "PluginControl/${plugin.version} (${server.platform}:${server.version}${data})";

    @Value(name = "cache", comment={
            "",
            "Configuration of the cache"
    })
    private CacheConfiguration cache = new CacheConfiguration();

    @Value(name="install", comment={
            "",
            "Configuration for the installation."
    })
    private InstallConfiguration install = new InstallConfiguration();

    @Value(name = "metrics", comment = {
            "",
            "The configuration of for the McStats implementation."
    })
    private MetricsConfiguration metrics = new MetricsConfiguration();

    @Value(name="debug", comment={
            "",
            "Enables the debug mode: Forced usage of verbose commands."
    })
    private boolean verbose = false;

    /**
     * Creates the default User-Interface uisettings.
     *
     * @return The default uisettings.
     */
    private Map<String, UserInterface> getDefaultUISettings() {
        Map<String, UserInterface> result = new HashMap<>();

        result.put("default", new UserInterface(19, UserInterface.PaginationShowPolicy.NEEDED));
        result.put("console", new UserInterface(-1, UserInterface.PaginationShowPolicy.NEVER));

        return result;
    }

    /**
     * @return The file where the localization is located.
     */
    public File getLocalizationFile() {
        return new File(PluginManager.getInstance().getPlatform().getConfigurationDirectory(), localization);
    }

    /**
     * Returns the user inteface settings for the given executor.
     *
     * @param executor The executor who has executed the command.
     * @return Its user interface settings.
     */
    public UserInterface getUserInterfaceSettings(CommandExecutor executor) {
        if (!executor.isPlayer()) {
            if (!uisettings.containsKey("console")) {
                uisettings.put("console", new UserInterface(-1, UserInterface.PaginationShowPolicy.NEVER));
            }

            return uisettings.get("console");
        } else if (!uisettings.containsKey(executor.getName())) {
            if (!uisettings.containsKey("default")) {
                uisettings.put("default", new UserInterface(19, UserInterface.PaginationShowPolicy.NEEDED));
            }

            return uisettings.get("default");
        } else {
            return uisettings.get(executor.getName());
        }
    }

    /**
     * Checks if the repository is allowed.
     * @return true if the repository can be added.
     */
    public boolean isAllowedRepository(Repository repository) {
        if (this.repositoryConfiguration.allowUnknown) return true;
        return this.repositoryConfiguration.repositories.containsKey(repository.getName());
    }

    /**
     * Retrieves the file of the configuration.
     * @param repository The repository-object that needs the configuration.
     * @return The repository configuration.
     */
    public File getRepositoryConfiguration(Repository repository) {
        String name = this.repositoryConfiguration.repositories.get(repository.getName());
        if (name == null) name = repository.getName().toLowerCase() + ".yml";

        return new File(PluginManager.getInstance().getPlatform().getConfigurationDirectory(), name);
    }

    /**
     * @return Returns the configuration of the proxy.
     */
    public ProxyConfiguration getProxyConfiguration() {
        return this.proxy;
    }

    /**
     * @return Returns the configuration of the cache.
     */
    public CacheConfiguration getCacheConfiguration() {
        return this.cache;
    }

    /**
     * @return Returns the configuration for the installer.
     */
    public InstallConfiguration getInstallConfiguration() {
        return this.install;
    }

    /**
     * @return Should all verbosable commands be verbose?
     */
    public boolean isVerbose() {
        return this.verbose;
    }

    /**
     * @return The user agent.
     */
    public String getUserAgent() {
        return this.useragent;
    }

    /**
     * @return The configuration for metrics.
     */
    public MetricsConfiguration getMetricsConfiguration() {
        return this.metrics;
    }

}
