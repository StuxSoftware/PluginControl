package net.stuxcrystal.pluginmanager.configuration;

import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.commandhandler.TranslationHandler;
import net.stuxcrystal.pluginmanager.PluginManager;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Localization class for Bukkit.
 */
public class Localization implements TranslationHandler {

    /**
     * The actual language file.
     */
    private Map<String, String> configuration;

    /**
     * The default localization.
     */
    private final Map<String, String> default_configuration;

    /**
     * The server manager.
     */
    private final PluginManager manager;

    /**
     * Initializes the command handler.
     *
     * @param platform     The manager this plugin in running on.
     * @param localization The file where the localization is stored.
     */
    public Localization(PluginManager platform, File localization) {
        this.default_configuration = this.loadDefaultLocalization(platform);
        if (localization != null) update(localization);
        this.manager = platform;
    }

    /**
     * Returns the translation for the CommandHandler.
     *
     * @param sender The sender that sent the command.
     * @param key    The Key of the translation.
     * @return
     */
    @Override
    public String getTranslation(CommandExecutor sender, String key) {
        return translate(key);
    }

    /**
     * Performs the translation.
     *
     * @param key    The translation key.
     * @param format The arguments to format the string.
     * @return The translation string.
     */
    public String translate(String key, Object... format) {
        Map<String, String> formatMap = new HashMap<String, String>();
        formatMap.putAll(FormatUtils.COLOR_MAP);
        FormatUtils.mergeArray(formatMap, "args", format);
        String raw = this.configuration.get(key);
        if (raw == null) raw = this.default_configuration.get(key);
        if (raw == null) raw = key;
        return FormatUtils.format(raw, formatMap);
    }

    /**
     * Parses the localization file.
     * @param br The reader reading the configuration.
     * @return A map containing the configuration.
     */
    private Map<String, String> parseConfiguration(BufferedReader br) {
        Map<String, String> result = new HashMap<String, String>();

        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.split("#", 2)[0];
                if (!line.contains(":"))
                    continue;

                String[] data = line.split(":", 2);
                result.put(data[0], data[1].trim());
            }
        } catch (IOException e) {
            this.manager.getLogger().severe("[Localization] Failed to read localization file.");
            e.printStackTrace();
            return Collections.emptyMap();
        }

        return result;
    }

    /**
     * Updates the localization configuration.
     *
     * @param br The BufferedReader that reads the configuration.
     */
    public void update(BufferedReader br) {
        this.configuration = parseConfiguration(br);
    }

    /**
     * Updates the localization.
     *
     * @param localization The file to the localization file.
     */
    public void update(File localization) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(localization));
        } catch (IOException e) {
            this.manager.getLogger().severe("[Localization] Failed to initialize Localization file.");
            e.printStackTrace();
            return;
        }

        update(br);

        try {
            br.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Loads the default localization file.
     * @param manager The plugin manager object.
     */
    private Map<String, String> loadDefaultLocalization(PluginManager manager) {
        InputStream resource = manager.getPlatform().getResource("localization.lang");

        if (resource == null) {
            manager.getLogger().log(Level.SEVERE, "Failed to load default localization.");
            manager.getLogger().log(Level.SEVERE, "You will not be able to use the plugin properly.");
            return Collections.emptyMap();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(resource));
        Map<String, String> result = this.parseConfiguration(br);
        try { br.close(); } catch (IOException ignored) {}
        return result;
    }
}
