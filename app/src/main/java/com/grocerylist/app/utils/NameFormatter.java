package com.grocerylist.app.utils;

import java.util.Locale;

/**
 * Display formatting for item names. Names are stored as entered; this only affects
 * how they are shown, so stored data and the purchase analytics are unchanged.
 */
public final class NameFormatter {

    private static final Locale DANISH = Locale.forLanguageTag("da-DK");

    private NameFormatter() {
        throw new AssertionError("NameFormatter cannot be instantiated");
    }

    /**
     * Upper-cases the first letter and leaves the rest untouched:
     * "æg" -> "Æg", "hakket oksekød" -> "Hakket oksekød", "iPhone-oplader" -> "IPhone-oplader".
     */
    public static String capitalizeFirst(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        int firstCodePointLength = Character.charCount(name.codePointAt(0));
        return name.substring(0, firstCodePointLength).toUpperCase(DANISH)
                + name.substring(firstCodePointLength);
    }
}