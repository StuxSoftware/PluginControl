package net.stuxcrystal.pluginmanager;

import net.stuxcrystal.commandhandler.CommandExecutor;
import net.stuxcrystal.pluginmanager.configuration.PluginConfiguration;

/**
 * Classes for the user inteface.
 */
public class UserInterface {

    private PluginManager manager;

    public UserInterface(PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Performs a paginated print.
     *
     * @param headerKey The transl
     * @param messages
     */
    public void printPaginated(CommandExecutor executor, String headerKey, int page, String[] messages) {
        int lines = this.manager.getConfiguration().getUserInterfaceSettings(executor).pagination_lines;

        if (lines == -1) {
            printUnpaginated(executor, headerKey, messages);
            return;
        }

        int max_pages = (int) Math.ceil(((double) messages.length) / lines);

        if (page < 0 || page > max_pages)
            throw new IllegalArgumentException("Invalid page number");

        executor.sendMessage(
                getHeader(this.manager.getConfiguration().getUserInterfaceSettings(executor), headerKey, page, max_pages)
        );

        for (int i = (page - 1) * lines; i < Math.min(messages.length, page * lines); i++) {
            executor.sendMessage(messages[i]);
        }
    }

    public void printUnpaginated(CommandExecutor executor, String headerKey, String[] messages) {
        executor.sendMessage(
                getHeader(this.manager.getConfiguration().getUserInterfaceSettings(executor), headerKey, 1, 1)
        );
        executor.sendMessage(messages);

    }

    public String getHeader(PluginConfiguration.UserInterface settings, String key, int cur_page, int max_page) {
        if (settings.forcePageNumbers == PluginConfiguration.UserInterface.PaginationShowPolicy.ALWAYS)
            return manager.translate("ui.header.paginated", manager.translate(key), "" + cur_page, "" + max_page);

        if (max_page > 1 || settings.forcePageNumbers != PluginConfiguration.UserInterface.PaginationShowPolicy.NEVER)
            return manager.translate("ui.header.paginated", manager.translate(key), "" + cur_page, "" + max_page);
        else
            return manager.translate("ui.header.default", manager.translate(key));
    }

}
