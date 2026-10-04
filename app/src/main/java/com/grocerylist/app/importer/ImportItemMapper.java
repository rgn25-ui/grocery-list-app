package com.grocerylist.app.importer;

import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.utils.CategoryPredictor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts validated import items into GroceryItems the same way the add-item dialog does:
 * Danish number format, the app's unit names and a predicted category.
 */
public final class ImportItemMapper {

    static final String DEFAULT_UNIT = "stk";

    private ImportItemMapper() {
    }

    public static GroceryItem toGroceryItem(ImportItem item, String listId) {
        GroceryItem groceryItem = new GroceryItem(listId, item.getName());
        groceryItem.setQuantity(formatNumber(item.getQuantity()));
        groceryItem.setUnit(mapUnit(item.getUnit()));
        groceryItem.setNotes(item.getNote() == null ? "" : item.getNote());
        groceryItem.setCategory(CategoryPredictor.predictCategory(item.getName()).name());
        groceryItem.setOnOffer(item.isOnOffer());
        groceryItem.setPrice(item.getPrice() == null ? "" : formatPrice(item.getPrice()));
        return groceryItem;
    }

    /** "2 stk", "0,5 kg", "2 L" - for the confirmation screen. */
    public static String formatAmount(ImportItem item) {
        return formatNumber(item.getQuantity()) + " " + mapUnit(item.getUnit());
    }

    /** Maps import units to the values in R.array.units_array. */
    static String mapUnit(String importUnit) {
        if (importUnit == null) {
            return DEFAULT_UNIT;
        }
        return "l".equals(importUnit) ? "L" : importUnit;
    }

    /** 2 -> "2", 0.5 -> "0,5", 1.25 -> "1,25". */
    static String formatNumber(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 0) {
            stripped = stripped.setScale(0, RoundingMode.UNNECESSARY);
        }
        return stripped.toPlainString().replace('.', ',');
    }

    /** 35 -> "35", 34.95 -> "34,95", 29.5 -> "29,50". The list view adds " kr". */
    public static String formatPrice(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}