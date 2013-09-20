/*
 * This file is used to be part in "PluginControl".
 * ------------------------------------------------
 *
 * The author allowed to use this code in a plugin.
 */
package net.stuxcrystal.pluginmanager.configuration;

import net.stuxcrystal.commandhandler.utils.MessageColor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to format localization.
 */
public class FormatUtils {

    /**
     * Map for colors.
     */
    public static final Map<String, String> COLOR_MAP = createColorMap();

    public static final Map<String, String> CHAR_MAP = createCharMap();

    /**
     * Creates the color map.
     *
     * @return The map of colors.
     */
    private static Map<String, String> createColorMap() {
        Map<String, String> result = new HashMap<>();

        for (Enum<?> enumerators : MessageColor.values()) {
            if (((MessageColor) enumerators).isFormat())
                result.put("format:" + enumerators.name().toLowerCase(), enumerators.toString());
            else
                result.put("color:" + enumerators.name().toLowerCase(), enumerators.toString());
        }

        result.put("color:red", MessageColor.RED.toString());
        result.put("color:blue", MessageColor.BLUE.toString());
        result.put("color:green", MessageColor.GREEN.toString());
        result.put("color:aqua", MessageColor.AQUA.toString());
        result.put("color:gray", MessageColor.GRAY.toString());
        result.put("color:grey", MessageColor.GRAY.toString());
        result.put("color:light_grey", MessageColor.GRAY.toString());
        result.put("color:dark_grey", MessageColor.DARK_GRAY.toString());

        return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> createCharMap() {
        Map<String, String> result = new HashMap<String, String>();

        result.put("char:space", " ");
        result.put("char:tab", "\t");

        return Collections.unmodifiableMap(result);
    }

    /**
     * Merges an array into a format map.
     *
     * @param format  The format map.
     * @param prefix  The prefix the array elements should be given.
     * @param objects The objects.
     */
    public static void mergeArray(Map<String, String> format, String prefix, Object... objects) {
        for (int index = 0; index < objects.length; index++) {
            Object object = objects[index];
            if (object == null)
                object = "null";

            format.put(prefix + ":" + index, object.toString());
        }
    }

    /**
     * Formats a format string.
     *
     * @param raw    The raw string.
     * @param format The format map.
     * @return The formatted string.
     */
    public static String format(String raw, Map<String, String> format) {
        String current = raw;
        for (Map.Entry<String, String> entry : format.entrySet()) {
            current = current.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        return current;
    }

}
