package net.stuxcrystal.pluginmanager.io;

import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.configuration.FormatUtils;
import net.stuxcrystal.pluginmanager.configuration.PluginConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class have to be used to connect through a
 * socket or to connect to a page.
 */
public class Connector {

    private final PluginManager manager;

    public Connector(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Parses the proxy configuration.
     * @return The proxy.
     */
    private Proxy getConfiguratedProxy() {
        PluginConfiguration.ProxyConfiguration configuration = this.manager.getConfiguration().getProxyConfiguration();

        if (configuration.type == PluginConfiguration.ProxyConfiguration.ProxyType.SYSTEM)
            return null;

        if (configuration.type == PluginConfiguration.ProxyConfiguration.ProxyType.NONE)
            return Proxy.NO_PROXY;

        InetSocketAddress address = new InetSocketAddress(configuration.host, configuration.port);
        if (configuration.type == PluginConfiguration.ProxyConfiguration.ProxyType.SOCKS)
            return new Proxy(Proxy.Type.SOCKS, address);

        if (configuration.type == PluginConfiguration.ProxyConfiguration.ProxyType.HTTP)
            return new Proxy(Proxy.Type.HTTP, address);

        return null; // Impossible...
    }

    /**
     * Creates a new socket that uses the configuration proxy configuration.
     * @return The new socket.
     * @throws IOException if an I/O-Operation fails.
     */
    public Socket newSocket() throws IOException {
        Proxy proxy = this.getConfiguratedProxy();
        if (proxy == null)
            return new Socket();
        return new Socket(proxy);
    }

    /**
     * Opens a connection to the web server using the given URL.
     * @param url The url to browse to.
     * @return A {@link URLConnection} that uses the proxy specified by the user.
     * @throws IOException If an I/O Operation fails.
     */
    public URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    /**
     * Opens a connection using a user-agent.
     *
     * @param url       The URL to connect to.
     * @param userAgent The agent to transmit.
     * @return A URL-Connection using the given User-Agent.
     * @throws IOException If an I/O-Operation fails.
     */
    public URLConnection openConnection(URL url, String userAgent) throws IOException {
        String rawAgent = this.manager.getConfiguration().getUserAgent();

        HashMap<String, String> format = new HashMap<>();
        format.put("plugin.version", this.manager.getVersion().getVersion());
        format.put("server.platform", this.manager.getPlatformName());
        format.put("server.version", this.manager.getPlatform().getPlatformVersion());

        if (!(userAgent == null || userAgent.trim().isEmpty())) {
            format.put("data", "; " + userAgent.trim());
        } else {
            format.put("data", "");
        }

        String agent = FormatUtils.format(rawAgent, format);

        return openConnectionRaw(url, agent);
    }

    /**
     * Opens a connection to a web-server using the given URL submitting the provided user-agent.
     * @param url       The URL to connect to.
     * @param userAgent The User-Agent to transmit.
     * @return The URL-Connection used.
     * @throws IOException If an I/O-Operation fails.
     */
    private URLConnection openConnectionRaw(URL url, String userAgent) throws IOException {
        Proxy proxy = this.getConfiguratedProxy();
        URLConnection connection = null;
        if (proxy == null)
            connection = url.openConnection();
        else
            connection = url.openConnection(proxy);

        connection.setRequestProperty("User-Agent", userAgent);

        return connection;
    }

}
