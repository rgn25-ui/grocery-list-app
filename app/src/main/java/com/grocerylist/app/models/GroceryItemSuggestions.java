package com.grocerylist.app.models;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages grocery item suggestions loaded from JSON resource file
 * Provides autocomplete functionality for Danish grocery items
 */
public class GroceryItemSuggestions {

    private static final String TAG = "GroceryApp";

    private GroceryItemSuggestions() {
        // Utility class - instantiation not allowed
    }

    public static class Suggestion {
        public final String title;
        public final String categoryDisplay;
        public Suggestion(String title, String categoryDisplay) {
            this.title = title;
            this.categoryDisplay = categoryDisplay;
        }
    }

    private static List<Suggestion> suggestions = null;
    private static boolean isInitialized = false;

    /**
     * Initialize suggestions from JSON resource file
     * Call this once during app initialization
     */
    public static void initialize(Context context) {
        if (isInitialized) {
            return;
        }

        try {
            // Read JSON file from res/raw
            @SuppressLint("DiscouragedApi") InputStream inputStream = context.getResources().openRawResource(
                    context.getResources().getIdentifier(
                            "grocery_suggestions",
                            "raw",
                            context.getPackageName()
                    )
            );

            InputStreamReader reader = new InputStreamReader(inputStream);

            // Parse JSON using Gson
            Gson gson = new Gson();
            Type listType = new TypeToken<List<JsonSuggestion>>() {}.getType();
            List<JsonSuggestion> jsonSuggestions = gson.fromJson(reader, listType);

            // Convert to Suggestion objects
            suggestions = new ArrayList<>();
            for (JsonSuggestion jsonSuggestion : jsonSuggestions) {
                suggestions.add(new Suggestion(jsonSuggestion.title, jsonSuggestion.category));
            }

            reader.close();
            isInitialized = true;

            android.util.Log.d(TAG, "✅ Loaded " + suggestions.size() + " grocery suggestions");

        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Failed to load grocery suggestions", e);
            // Fallback to empty list
            suggestions = new ArrayList<>();
            isInitialized = true;
        }
    }

    /**
     * Get suggestions matching the query
     * @param query Search query (minimum 3 characters)
     * @param limit Maximum number of results
     * @return List of matching suggestions
     */
    public static List<Suggestion> getSuggestions(String query, int limit) {
        if (!isInitialized || suggestions == null) {
            android.util.Log.w(TAG, "⚠️ Suggestions not initialized yet");
            return Collections.emptyList();
        }

        if (query == null || query.length() < 3) {
            return Collections.emptyList();
        }

        String lowerQuery = query.toLowerCase();
        List<Suggestion> filtered = new ArrayList<>();

        for (Suggestion suggestion : suggestions) {
            if (suggestion.title.toLowerCase().contains(lowerQuery)) {
                filtered.add(suggestion);
            }
        }

        // Sort by priority: starts with query first, then alphabetically
        filtered.sort((s1, s2) -> {
            boolean s1Starts = s1.title.toLowerCase().startsWith(lowerQuery);
            boolean s2Starts = s2.title.toLowerCase().startsWith(lowerQuery);

            if (s1Starts && !s2Starts) return -1;
            if (!s1Starts && s2Starts) return 1;

            return s1.title.compareToIgnoreCase(s2.title);
        });

        // Return limited results
        return filtered.size() <= limit ? filtered : filtered.subList(0, limit);
    }

    /**
     * Helper class for JSON deserialization
     */
    private static class JsonSuggestion {
        @SuppressWarnings("unused") // Field used by Gson via reflection
        String title;
        @SuppressWarnings("unused") // Field used by Gson via reflection
        String category;
    }
}