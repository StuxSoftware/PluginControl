package net.stuxcrystal.pluginmanager.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 * The string utilities. Needed in this plugin.
 */
public final class StringUtil {

    /**
     * Joins a collection of strings.
     * @param collection The collection to join.
     * @param separator  The separator.
     * @param <T>
     * @return The joined string.
     */
    public static <T extends CharSequence> String join(Collection<T> collection, String separator) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (CharSequence sequence : collection) {
            if (!first) {
                builder.append(separator);
            }
            builder.append(sequence);
            first = false;
        }
        return builder.toString();
    }

    /**
     * Joins an array of CharSequences (or strings).
     * @param strings  The strings to join.
     * @param separtor The separator.
     * @return The joined string.
     */
    public static <T extends CharSequence> String join(T[] strings, String separtor) {
        return join(Arrays.asList(strings), separtor);
    }

}
