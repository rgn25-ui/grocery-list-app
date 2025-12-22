package com.grocerylist.app.models;

import java.util.List;

@SuppressWarnings("unused")  // Used by Gson for JSON deserialization
public class SyncData {
    private final List<GroceryList> lists;
    private List<GroceryItem> items;

    public SyncData(List<GroceryList> lists, List<GroceryItem> items) {
        this.lists = lists;
        this.items = items;
    }
    
    // Getters and setters
    public List<GroceryList> getLists() { return lists; }

    public List<GroceryItem> getItems() { return items; }
    public void setItems(List<GroceryItem> items) { this.items = items; }
}