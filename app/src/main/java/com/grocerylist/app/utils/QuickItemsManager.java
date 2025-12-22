package com.grocerylist.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuickItemsManager {
    private static final String PREFS_NAME = "quick_items_prefs";
    private static final String KEY_QUICK_ITEMS = "quick_items";
    private static final String KEY_VERSION = "quick_items_version";
    private static final int CURRENT_VERSION = 2; // Increment this to force reset
    private static final int MAX_QUICK_ITEMS = 20;
    private static final String DELIMITER = "|||";

    private final SharedPreferences prefs;

    public QuickItemsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkAndMigrate();
    }

    /**
     * Check version and migrate/reset if needed
     */
    private void checkAndMigrate() {
        int savedVersion = prefs.getInt(KEY_VERSION, 0);

        if (savedVersion < CURRENT_VERSION) {
            // Clear old data and reset to defaults
            prefs.edit()
                    .remove(KEY_QUICK_ITEMS)
                    .putInt(KEY_VERSION, CURRENT_VERSION)
                    .apply();
        }
    }

    /**
     * Get all quick items. Returns default items if none are saved.
     */
    public List<String> getQuickItems() {
        String itemsString = prefs.getString(KEY_QUICK_ITEMS, null);

        if (itemsString == null || itemsString.isEmpty()) {
            return getDefaultQuickItems();
        }

        List<String> items;
        if (itemsString.contains(DELIMITER)) {
            items = new ArrayList<>(Arrays.asList(itemsString.split(java.util.regex.Pattern.quote(DELIMITER))));
        } else {
            items = new ArrayList<>();
            items.add(itemsString);
        }

        // Sort with Danish locale and case-insensitive
        items.sort(new java.util.Comparator<String>() {
            private final java.text.Collator danishCollator = java.text.Collator.getInstance(new java.util.Locale("da", "DK"));

            @Override
            public int compare(String s1, String s2) {
                return danishCollator.compare(s1, s2);
            }
        });

        return items;
    }

    /**
     * Add a new quick item
     */
    public boolean addQuickItem(String itemName) {
        List<String> items = getQuickItems();

        if (items.size() >= MAX_QUICK_ITEMS) {
            return false;
        }

        if (items.contains(itemName)) {
            return true;
        }

        items.add(itemName);
        Collections.sort(items);
        saveQuickItems(items);
        return true;
    }

    /**
     * Remove a quick item
     */
    public void removeQuickItem(String itemName) {
        List<String> items = getQuickItems();
        items.remove(itemName);
        Collections.sort(items);
        saveQuickItems(items);
    }

    /**
     * Check if an item is already a quick item
     */
    public boolean isQuickItem(String itemName) {
        return getQuickItems().contains(itemName);
    }

    /**
     * Save quick items to preferences
     */
    private void saveQuickItems(List<String> items) {
        if (items.isEmpty()) {
            prefs.edit().remove(KEY_QUICK_ITEMS).apply();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) {
                sb.append(DELIMITER);
            }
        }
        prefs.edit().putString(KEY_QUICK_ITEMS, sb.toString()).apply();
    }

    /**
     * Reset to default quick items
     */
    public void resetToDefaults() {
        List<String> defaults = getDefaultQuickItems();
        saveQuickItems(defaults);
    }

    /**
     * Default quick items for new users
     */
    private List<String> getDefaultQuickItems() {
        List<String> defaults = new ArrayList<>(Arrays.asList(
                "Mælk", "Juice", "Rugbrød", "Boller", "Gulerod",
                "Tomater", "Spidskål", "Agurk", "Æbler", "Pasta", "Choko", "Skyr"
        ));

        // Sort with Danish locale
        defaults.sort(new java.util.Comparator<String>() {
            private final java.text.Collator danishCollator = java.text.Collator.getInstance(new java.util.Locale("da", "DK"));

            @Override
            public int compare(String s1, String s2) {
                return danishCollator.compare(s1, s2);
            }
        });

        return defaults;
    }
}