package net.stuxcrystal.pluginmanager.utils;

import java.util.List;

/**
 * Utilities for lists.
 */
public class ListUtils {

    /**
     * Checks if at least one of multiple lists contains the item.
     * @param check The item to check.
     * @param lists The lists to check.
     * @param <T>   The type of the item.
     * @return true if one or more list contained the item.
     */
    public static <T> boolean contains(T check, List<T>... lists) {
        for (List<? super T> list : lists) {
            if (list.contains(check)) return true;
        }

        return false;
    }
}
