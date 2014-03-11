package ru.vang.songoftheday.util;

public final class StringUtils {

    private static final int SEPARATOR_OFFSET = 1;

    public static String extractValueFromUrl(final String url, final String name) {
        if (url == null || name == null) {
            throw new NullPointerException(String.format(
                    "Some arguments are null: url=%s, name=%s", url, name));
        }

        final int startIndex = url.indexOf(name) + name.length() + SEPARATOR_OFFSET;
        final char[] urlChars = url.toCharArray();
        final StringBuilder builder = new StringBuilder();
        final int length = urlChars.length;
        for (int i = startIndex; i < length && urlChars[i] != '&'; i++) {
            builder.append(urlChars[i]);
        }

        return builder.toString();
    }
}
