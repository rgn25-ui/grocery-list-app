package com.grocerylist.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.UUID;

@Entity(
        tableName = "grocery_items",
        foreignKeys = @ForeignKey(
                entity = GroceryList.class,
                parentColumns = "id",
                childColumns = "listId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("listId")} // ADDED: Index on listId to fix foreign key warning
)
public class GroceryItem implements Serializable {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    @PrimaryKey
    @NonNull
    private String id;

    private String listId;
    private String name;
    private String quantity;
    private String unit;
    private String notes;
    private String category;
    private boolean isCompleted;
    private int priority;
    @SuppressWarnings("unused") // Room uses field via reflection
    private long createdAt;
    private long updatedAt;
    @SuppressWarnings("unused") // Room uses field via reflection
    private boolean isDeleted;
    private boolean onOffer;
    private String price;

    // Room will use this no-arg constructor
    public GroceryItem() {
        this.id = UUID.randomUUID().toString();
        this.quantity = "";
        this.unit = "";
        this.notes = "";
        this.category = Category.DIVERSE.name(); // CHANGED: Use DIVERSE instead of OTHER
        this.isCompleted = false;
        this.priority = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
        this.onOffer = false;        // Default: not on offer
        this.price = "";             // Default: no price
    }

    // Mark convenience constructor to be ignored by Room
    @Ignore
    public GroceryItem(String listId, String name) {
        this();
        this.listId = listId;
        this.name = name;
    }

    // Getters and setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public boolean getIsDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public boolean isOnOffer() { return onOffer; }
    public void setOnOffer(boolean onOffer) { this.onOffer = onOffer; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}