package com.grocerylist.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "grocery_lists")
public class GroceryList implements Serializable {
    @SuppressWarnings("unused") // Room uses field via reflection
    private static final long serialVersionUID = 1L;

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    @SuppressWarnings("unused") // Room uses field via reflection
    private String userId;

    private String category;
    @SuppressWarnings("unused") // Room uses field via reflection
    private long createdAt;
    private long updatedAt;
    @SuppressWarnings("unused") // Room uses field via reflection
    private boolean isDeleted;

    // Room will use this no-arg constructor
    public GroceryList() {
        this.id = UUID.randomUUID().toString();
        this.category = "REMA"; // Default to REMA
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
    }

    // Mark convenience constructor to be ignored by Room
    @Ignore
    public GroceryList(String name) {
        this();
        this.name = name;
    }

    // Getters and setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public boolean getIsDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}