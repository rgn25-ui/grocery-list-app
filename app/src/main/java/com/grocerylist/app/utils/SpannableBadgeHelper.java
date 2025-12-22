package com.grocerylist.app.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.grocerylist.app.models.ListCategory;

/**
 * Helper class for creating colored badge text using SpannableString
 * Used for list category badges in the app (e.g., "[R] Rema: List Name")
 * Eliminates duplicated badge creation code across GroceryListAdapter, MainActivity, and ListDetailActivity
 */
public class SpannableBadgeHelper {

    private SpannableBadgeHelper() {
        throw new AssertionError("SpannableBadgeHelper cannot be instantiated");
    }

    /**
     * Creates a formatted badge for list display
     * Format: "[R] Rema: List Name" where [R] has colored background
     *
     * @param category The list category (determines badge color and letter)
     * @param listName The name of the list to display
     * @return SpannableString with formatted badge
     */
    public static SpannableString createListCategoryBadge(ListCategory category, String listName) {
        String letter = " " + category.getLetter() + " ";
        String storeName = category.getDisplayName();
        String fullText = letter + " " + storeName + ": " + listName;

        return applyBadgeStyle(fullText, letter.length(), category.getBackgroundColor());
    }

    /**
     * Creates a formatted badge for dialog titles
     * Format: "[R] Action Text" where [R] has colored background
     *
     * @param category The list category (determines badge color and letter)
     * @param actionText The action text to display (e.g., "Rename List")
     * @return SpannableString with formatted badge
     */
    public static SpannableString createDialogTitleBadge(ListCategory category, String actionText) {
        String letter = " " + category.getLetter() + " ";
        String fullText = letter + " " + actionText;

        return applyBadgeStyle(fullText, letter.length(), category.getBackgroundColor());
    }

    /**
     * Applies badge styling to the first portion of text
     * - Colored background
     * - White foreground text
     * - Bold typeface
     *
     * @param fullText The complete text to style
     * @param badgeLength The length of the badge portion to style
     * @param backgroundColor The background color for the badge
     * @return SpannableString with applied styling
     */
    private static SpannableString applyBadgeStyle(String fullText, int badgeLength, int backgroundColor) {
        SpannableString spannable = new SpannableString(fullText);

        // Apply background color to badge
        spannable.setSpan(
                new BackgroundColorSpan(backgroundColor),
                0,
                badgeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Apply white foreground color to badge
        spannable.setSpan(
                new ForegroundColorSpan(Color.WHITE),
                0,
                badgeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Make badge text bold
        spannable.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                badgeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return spannable;
    }
}