package net.stuxcrystal.pluginmanager.repository.packaged.BukGet;

import net.stuxcrystal.configuration.annotations.Configuration;
import net.stuxcrystal.configuration.annotations.Value;

/**
 * Stores the settings of BukGet.
 */
@Configuration
public class BukGetSettings {

    /**
     * A list of existing api-servers.
     */
    public static enum Server {
        AUTO   ("api.bukget.org"),
        DALLAS ("dallas.api.bukget.org"),
        PARIS  ("paris.api.bukget.org");

        private final String domain;

        private Server(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return this.domain;
        }
    }

    @Value(name = "server", comment= {
            "The repository chooses its server automatically by default, but you",
            "can modify the behavior using this setting.",
            "  AUTO     Automatically choose the API-Server.",
            "  DALLAS   Use the \"Dallas\" API-Server.",
            "  PARIS    The the \"Paris\" API-Server."
    })
    Server server = Server.AUTO;

    @Value(name = "type", comment = {
            "",
            "Usually you don't want to change this value."
    })
    String serverType = "bukkit";

    @Value(name = "use-bukget-dl", comment = {
            "",
            "If this value is true, the download URL of BukGet will be used."
    })
    boolean useBukGetDownload = false;

    @Value(name = "cache-headers", comment = {
            "",
            "Should the headers be cached."
    })
    boolean cacheHeaders = false;

    @Value(name = "no-fields", comment = {
            "",
            "Activate this value to force BukGet to send all data about the plugin.."
    })
    boolean noFields = false;

    @Value(name = "user-agent", comment = {
            "",
            "The User-Agent property. Please note that the user-agent.",
            "Plese note that the user-agent string will always start with the string given in",
            "global configuration file of PluginControl."
    })
    String useragent = "Repository:BukGet";
}
