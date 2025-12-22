package com.grocerylist.app.utils;

/**
 * Application-wide constants
 * Unified constants file - replaces ApiConstants, DatabaseConstants,
 * PreferencesConstants, SortConstants, and UiConstants
 */
public class Constants {

    // ===== API CONSTANTS =====

    public static final String BASE_URL = "https://grocery-backend-285291610580.europe-west1.run.app/";
    public static final String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // ===== DATABASE CONSTANTS =====

    public static final String DATABASE_NAME = "grocery_database";

    // ===== PREFERENCES CONSTANTS =====

    public static final String PREFS_NAME = "grocery_prefs";
    public static final String PREF_LAST_SYNC = "last_sync_time";

    // ===== SORT CONSTANTS =====

    public static final int SORT_BY_NAME = 0;      // Alphabetical sorting
    public static final int SORT_BY_REMA1000 = 1;  // Rema1000 store layout sorting

    private Constants() {
        throw new AssertionError("AppConstants cannot be instantiated");
    }
}