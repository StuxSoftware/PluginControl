package net.stuxcrystal.pluginmanager.utils;

import net.stuxcrystal.pluginmanager.Plugin;
import net.stuxcrystal.pluginmanager.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Helper for a dependency tree.
 */
public class DependencyUtils {

    /**
     * Calculates all dependent plugins.
     * @param plugin The plugin to check
     * @param soft   Calculates soft depencies.
     * @return A list of plugins that depends (indirectly) the given plugin.
     */
    public static List<Plugin> getDependentPluginTree(Plugin plugin, boolean soft) {
        return getDependentPluginTree(Arrays.asList(PluginManager.getInstance().getPlugins()), plugin, soft);
    }

    /**
     * Calculates all dependent plugins.
     * @param pluginList The list of plugins.
     * @param plugin     The plugin to to retrieve.
     * @param soft       Retrieve soft dependencies.
     * @return A list of dependent plugins.
     */
    private static List<Plugin> getDependentPluginTree(List<Plugin> pluginList, Plugin plugin, boolean soft) {

        List<Plugin> result = new ArrayList<Plugin>();
        List<Plugin> addedPlugins = new ArrayList<Plugin>();
        List<Plugin> current = new ArrayList<Plugin>();

        addedPlugins.add(plugin);

        do {
            result.addAll(addedPlugins);
            current.addAll(addedPlugins);
            addedPlugins.clear();

            for (Plugin currentPlugin : current) {
                addedPlugins.addAll(
                        getDependentPlugins(
                                pluginList, joinListsDistinct(result, current, addedPlugins), currentPlugin, soft
                        )
                );
            }

            current.clear();

        } while (addedPlugins.size() > 0);

        return result;

    }

    /**
     * Returns all plugins that depends on the given plugin.
     * @param pluginList All plugins on this server.
     * @param plugin     The plugin to get its dependent plugins.
     * @param soft       Calculate soft dependent plugins.
     */
    private static List<Plugin> getDependentPlugins(List<Plugin> pluginList, List<Plugin> ignore, Plugin plugin, boolean soft) {

        List<Plugin> output = new ArrayList<Plugin>();

        for (Plugin current : pluginList) {
            if (ignore.contains(current)) continue;

            if (current.getDependencies(soft).contains(plugin.getName())) {
                output.add(current);
            }
        }

        return output;

    }

    /**
     * Joins multiple list while remaining distinct.
     * @param lists An array of lists of plugins.
     * @return A distinct list
     */
    private static List<Plugin> joinListsDistinct(Collection<Plugin>... lists) {
        List<Plugin> result = new ArrayList<Plugin>();

        for (Collection<Plugin> list : lists) {
            for (Plugin plugin : list) {
                if (!result.contains(plugin)) result.add(plugin);
            }
        }

        return result;
    }

}
