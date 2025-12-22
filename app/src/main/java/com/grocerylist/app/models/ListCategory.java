package com.grocerylist.app.models;

public enum ListCategory {
    REMA("Rema", "R", 0xFF1976D2),      // Blue background
    COOP("Coop", "C", 0xFFD32F2F),      // Red background
    ANDRE("Andre", "?", 0xFF388E3C);    // Green background

    private final String displayName;
    private final String letter;
    private final int backgroundColor;

    ListCategory(String displayName, String letter, int backgroundColor) {
        this.displayName = displayName;
        this.letter = letter;
        this.backgroundColor = backgroundColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLetter() {
        return letter;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public static ListCategory getCategoryByName(String categoryName) {
        try {
            return ListCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            return REMA; // Default to REMA if invalid
        }
    }
}