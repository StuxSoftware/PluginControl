package net.stuxcrystal.pluginmanager.repository.packaged.BukGet;

import net.stuxcrystal.pluginmanager.PluginManager;
import net.stuxcrystal.pluginmanager.ServerPlatform;
import net.stuxcrystal.pluginmanager.io.Connector;
import net.stuxcrystal.pluginmanager.io.StreamUtilities;
import net.stuxcrystal.pluginmanager.repository.PluginInformation;
import net.stuxcrystal.pluginmanager.repository.Repository;
import net.stuxcrystal.pluginmanager.utils.JSONHelper;
import net.stuxcrystal.pluginmanager.utils.URLUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Repository to query DevBukkit using BukGet.
 */
public class BukGetRepository extends Repository<BukGetSettings> {

    /**
     * Returns the default query url.
     */
    private static final String SINGLE_PLUGIN_URL = "http://{0}/3/plugins/{1}/{2}/latest";

    /**
     * Download URL to bukget.
     */
    private static final String BUKGET_DOWNLOAD_URL = "http://{0}/3/plugins/{1}/{2}/latest/download";

    /**
     * Query to show all fields.
     */
    private static final String BUKGET_FIELD_QUERY =
            "?fields=plugin_name,authors,description,versions.version,versions.hard_dependencies,versions.download,versions.filename";


    /**
     * The name of the repository.
     * @return The internal name of the repository.
     */
    @Override
    public String getName() {
        return "DevBukkit";
    }

    @Override
    public String getDescription() {
        return "DevBukkit repository binding";
    }

    @Override
    public boolean isCompatible(ServerPlatform platform) {
        return platform.getPlatformName().equals("Bukkit");
    }

    @Override
    public Class<BukGetSettings> getSettingsClass() {
        return BukGetSettings.class;
    }

    @Override
    public PluginInformation getPluginInformation(String name) {
        name = this.getDownloadName(name);
        BukGetSettings settings = this.getSettings();

        URL url = this.getDataURL(name);

        JSONObject object;
        try {
            String content = this.downloadHeaders(settings, url);
            object = (JSONObject) new JSONParser().parse(content);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }

        // Load data.
        String realName = JSONHelper.getValue(object, "plugin_name");
        String version = JSONHelper.getValue(object, "versions", 0, "version");
        List<String> author = JSONHelper.getValue(object, "authors");
        String description = JSONHelper.getValue(object, "description");
        List<String> dependencies = JSONHelper.getValue(object, "versions", 0, "hard_dependencies");
        URL download;

        // This repository has a redirection page. To avoid extra traffic to BukGet, usage of this function
        // is disabled by default.
        if (settings.useBukGetDownload)
            download = URLUtils.constructURL(BUKGET_DOWNLOAD_URL, settings.server.getDomain(), settings.serverType, name);
        else
            download = URLUtils.constructURL((String) JSONHelper.getValue(object, "versions", 0, "download"));

        // The filename
        String filename = JSONHelper.getValue(object, "versions", 0, "filename");

        return new PluginInformation(
                this,
                realName, version,
                description, author,
                download, filename,
                dependencies
        );
    }

    /**
     * Returns the name of the plugin suitable for BukGet..
     * @param name The name of the plugin.
     * @return The name of the plugin.
     */
    private String getDownloadName(String name) {
        return name.toLowerCase().replace(' ', '-');
    }

    /**
     * Returns the URL of the plugin.
     * @param name The name of the plugin.
     * @return The URL of to the plugin data.
     */
    private URL getDataURL(String name) {
        BukGetSettings settings = this.getSettings();
        return URLUtils.getURLQuietly(URLUtils.constructURLAsString(
                SINGLE_PLUGIN_URL, settings.server.getDomain(), settings.serverType, name
        ) + (settings.noFields ? "" : BUKGET_FIELD_QUERY));
    }

    /**
     * Downloads the headers.
     * @param url the URL to download.
     * @return A string containing the header.
     */
    private String downloadHeaders(BukGetSettings settings, URL url) throws IOException {
        if (!settings.cacheHeaders) {
            Connector connector = PluginManager.getInstance().getConnector();
            return StreamUtilities.toString(
                    connector.openConnection(url, settings.useragent).getInputStream(), "UTF-8"
            );
        } else {
            File file = PluginManager.getInstance().getCache().downloadFile(url, settings.useragent);
            return StreamUtilities.toString(new FileInputStream(file), "UTF-8");
        }
    }


}