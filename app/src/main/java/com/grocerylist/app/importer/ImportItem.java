package com.grocerylist.app.importer;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One validated item from an import payload.
 * Quantity is a number of pieces when unit is null, otherwise an amount in that unit.
 */
public final class ImportItem {

    private final String name;
    private final BigDecimal quantity;
    private final String unit;      // null = pieces; otherwise g, kg, ml or l
    private final String note;      // null = no note
    private final BigDecimal price; // null = no price; per piece, or for the stated amount
    private final boolean onOffer;

    ImportItem(String name, BigDecimal quantity, String unit, String note,
               BigDecimal price, boolean onOffer) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.note = note;
        this.price = price;
        this.onOffer = onOffer;
    }

    public String getName() { return name; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getNote() { return note; }
    public BigDecimal getPrice() { return price; }
    public boolean isOnOffer() { return onOffer; }

    ImportItem withQuantity(BigDecimal newQuantity) {
        return new ImportItem(name, newQuantity, unit, note, price, onOffer);
    }

    /** True when everything except name and quantity matches, so two lines can be merged. */
    boolean hasSameDetails(ImportItem other) {
        boolean samePrice = price == null
                ? other.price == null
                : other.price != null && price.compareTo(other.price) == 0;
        return samePrice && onOffer == other.onOffer && Objects.equals(note, other.note);
    }
}
