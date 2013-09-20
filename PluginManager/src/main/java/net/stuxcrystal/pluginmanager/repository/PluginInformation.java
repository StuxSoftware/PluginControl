package net.stuxcrystal.pluginmanager.repository;

import java.net.URL;
import java.util.List;

/**
 * Information about the plugin.
 */
public class PluginInformation {

    /**
     * The name of the plugin.
     */
    private final String name;

    /**
     * The version of the plugin.<p />
     *
     * The value of this field is null if this value couldn't be retrieved.
     */
    private final String version;

    /**
     * The description of the plugin.<p />
     *
     * The value of this field is null if this value couldn't be retrieved.
     */
    private final String description;

    /**
     * The authors of the plugin.<p />
     *
     * The value of this field is null if this value couldn't be retrieved.
     */
    private final List<String> authors;

    /**
     * The URL to the Jar-File.<p />
     *
     * The value of this field is null if this value couldn't be retrieved.
     * This value is needed in the process of installing a plugin.
     */
    private final URL downloadUrl;

    /**
     * The actual filename of the resulting file.<p />
     *
     * If this value is null the filname of the download url will be used.
     */
    private final String filename;

    /**
     * The dependencies the plugin has.<p />
     *
     * The value of this field is null if this value couldn't be retrieved.
     * If this value couldn't be retrieved the plugin cannot make sure that
     * all dependencies that are required to run the plugin are installed.
     */
    private final List<String> dependencies;

    /**
     * The repository that provides the plugin information.
     */
    private final Repository repository;

    /**
     * Constructs the PluginInformation.
     * @param repository    The repository that provided the information.
     * @param name          The name of the plugin.
     * @param version       The version of the plugin or null if the version couldn't be retrieved.
     * @param authors       The authors of the plugin or null if the authors couldn't be retrieved.
     * @param downloadUrl   The download-URL of the plugin or null if no download-link can be provided.
     * @param filename      The filename of the file to output.
     * @param dependencies  The dependencies of the plugin or null if no dependencies can be provided.
     */
    public PluginInformation(Repository repository, String name, String version, String description, List<String> authors, URL downloadUrl, String filename, List<String> dependencies) {
        this.repository = repository;
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.downloadUrl = downloadUrl;
        this.filename = filename;
        this.dependencies = dependencies;
    }
    /**
     * Constructs the PluginInformation using the download-url as filename.
     * @param repository    The repository that provided the information.
     * @param name          The name of the plugin.
     * @param version       The version of the plugin or null if the version couldn't be retrieved.
     * @param authors       The authors of the plugin or null if the authors couldn't be retrieved.
     * @param downloadUrl   The download-URL of the plugin or null if no download-link can be provided.
     * @param dependencies  The dependencies of the plugin or null if no dependencies can be provided.
     */
    public PluginInformation(Repository repository, String name, String version, String description, List<String> authors, URL downloadUrl, List<String> dependencies) {
        this(repository, name, version, description, authors, downloadUrl, null, dependencies);
    }

    /**
     * @return The name of the plugin.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The version of the plugin or null if no version could be found.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The authors of the plugin or null if no authors can be provided.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * @return The download url or null if no download can be provided.
     */
    public URL getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * The filename or null if no filename or download-url is given.
     * @return A string containing the filename.
     */
    public String getFilename() {
        if (this.filename != null) return this.filename;
        if (this.downloadUrl != null) return this.downloadUrl.getFile();
        return null;
    }

    /**
     * @return The dependencies or null if no dependencies can be provided.
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    /**
     * @return The description of the plugin or null if no description can be provided.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The repository that downloaded the information.
     */
    public Repository getRepository() {
        return repository;
    }
}
