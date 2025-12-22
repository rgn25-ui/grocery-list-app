package com.grocerylist.app.models;

public enum Category {
    BROED("Brød", 0xFF6D4C41, "🥖"),           // Baguette emoji
    GROENGSAGER("Grøntsager", 0xFF2E7D32, "🥕"), // Carrot emoji
    FRUGT("Frugt", 0xFFF57C00, "🍎"),          // Apple emoji
    KOED("Kød", 0xFFC62828, "🥩"),             // Meat emoji
    PAALAEG("Pålæg", 0xFFE91E63, "🥪"),        // Sandwich emoji
    MEJERI("Mejeri", 0xFF1976D2, "🥛"),        // Milk emoji
    FROST("Frost", 0xFF3F51B5, "❄️"),          // Snowflake emoji
    TOERSTOF("Tørstof", 0xFF689F38, "🥫"),     // Canned food emoji
    DRIKKELSE("Drikkelse", 0xFF7B1FA2, "🥤"),  // Cup with straw emoji
    SNACKS("Snacks", 0xFFFF5722, "🍿"),        // Popcorn emoji
    DIVERSE("Diverse", 0xFF455A64, "📦");      // Package emoji

    private final String displayName;
    private final int color;
    private final String emoji;

    Category(String displayName, int color, String emoji) {
        this.displayName = displayName;
        this.color = color;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getSortOrder() {
        switch (this) {
            case BROED: return 1;        // Brød
            case GROENGSAGER: return 2;  // Grøntsager
            case FRUGT: return 3;        // Frugt
            case KOED: return 4;         // Kød
            case PAALAEG: return 5;      // Pålæg
            case MEJERI: return 6;       // Mejeri
            case FROST: return 7;        // Frost
            case TOERSTOF: return 8;     // Tørstof
            case DRIKKELSE: return 9;    // Drikkelse
            case SNACKS: return 10;      // Snacks
            case DIVERSE: return 11;     // Diverse
            default: return 12;
        }
    }

    public static Category getCategoryByName(String categoryName) {
        try {
            return Category.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            return DIVERSE;
        }
    }
}