package net.stuxcrystal.pluginmanager.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * Utils for URLs.
 */
public final class URLUtils {
    /**
     * Constructs urls.
     *
     * @param format The URL-Format.
     * @param values The values.
     * @return An URL-Object.
     */
    public static URL constructURL(String format, Object... values) {
        return getURLQuietly(constructURLAsString(format, values));


    }

    /**
     * Constructs urls as a string.
     *
     * @param format The default URL-Format.
     * @param values The values.
     * @return The formattes string.
     */
    public static String constructURLAsString(String format, Object... values) {
        String[] stringValues = new String[values.length];
        for (int i = 0; i<values.length; i++) {
            if (values[i] == null) {
                stringValues[i] = "null";
            } else {
                try {
                    stringValues[i] = URLEncoder.encode(values[i].toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    stringValues[i] = "null";
                }
            }
        }

        MessageFormat formatter = new MessageFormat(format);
        return formatter.format(stringValues);
    }

    /**
     * Makes an URL for a string.
     * @param url The URL as a string.
     * @return The url if the conversion worked or null if it failed.
     */
    public static URL getURLQuietly(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
