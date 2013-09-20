package net.stuxcrystal.pluginmanager.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Helper to get values from JSON.
 */
public final class JSONHelper {

    /**
     * Returns the value behind the path.
     * @param object The JSON-Object.
     * @param path   The path to the value needed.
     * @param <T>    The type of the value.
     * @return The object inside the path.
     */
    public static <T> T getValue(Object object, Object... path) {
        Object current = object;

        for (Object pathPart : path) {
            if (pathPart instanceof Integer) {
                current = ((JSONArray) current).get((int) pathPart);
            } else if (pathPart instanceof String) {
                current = ((JSONObject) current).get(pathPart);
            } else {
                throw new IllegalArgumentException("Cannot parse path.");
            }
        }
        return (T) current;
    }

}
