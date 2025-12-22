package com.grocerylist.app.repository;

import androidx.lifecycle.LiveData;

import com.grocerylist.app.database.GroceryDao;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;

import java.util.List;
import java.util.UUID;

/**
 * Handles all local database operations (Room database)
 * Responsible for CRUD operations on the local SQLite database
 */
public class LocalDataSource {
    private final GroceryDao groceryDao;

    public LocalDataSource(GroceryDao groceryDao) {
        this.groceryDao = groceryDao;
    }

    // ===== LIST OPERATIONS =====

    public LiveData<List<GroceryList>> getAllLists() {
        return groceryDao.getAllLists();
    }

    public void insertList(GroceryList list) {
        groceryDao.insertList(list);
    }

    public void deleteList(String listId, long timestamp) {
        groceryDao.deleteList(listId, timestamp);
    }

    public GroceryList getListById(String listId) {
        return groceryDao.getListById(listId);
    }

    // ===== ITEM OPERATIONS =====

    public LiveData<List<GroceryItem>> getItemsForList(String listId) {
        return groceryDao.getItemsForList(listId);
    }

    public void insertItem(GroceryItem item) {
        groceryDao.insertItem(item);
    }

    public void updateItem(GroceryItem item) {
        groceryDao.updateItem(item);
    }

    public void deleteItem(String itemId, long timestamp) {
        groceryDao.deleteItem(itemId, timestamp);
    }

    public GroceryItem getItemById(String itemId) {
        return groceryDao.getItemByIdSync(itemId);
    }

    public void clearCompletedItems(String listId) {
        groceryDao.clearCompletedItems(listId);
    }

    // ===== ITEM COUNT OPERATIONS =====

    public LiveData<Integer> getItemCountForList(String listId) {
        return groceryDao.getItemCountForListLive(listId);
    }

    // ===== BULK OPERATIONS =====

    public void deleteAllItems() {
        groceryDao.deleteAllItems();
    }

    public void deleteAllLists() {
        groceryDao.deleteAllLists();
    }

    // ===== DUPLICATE LIST OPERATION =====

    public String duplicateList(String originalListId, String newName, String category, String userId) {
        String newListId = UUID.randomUUID().toString();
        List<GroceryItem> originalItems = groceryDao.getItemsForListSync(originalListId);

        GroceryList newList = new GroceryList(newName);
        newList.setId(newListId);
        newList.setUserId(userId);
        newList.setCategory(category);
        groceryDao.insertList(newList);

        for (GroceryItem item : originalItems) {
            GroceryItem newItem = new GroceryItem(newListId, item.getName());
            newItem.setQuantity(item.getQuantity());
            newItem.setUnit(item.getUnit());
            newItem.setNotes(item.getNotes());
            newItem.setCategory(item.getCategory());
            newItem.setPriority(item.getPriority());
            groceryDao.insertItem(newItem);
        }

        return newListId;
    }
}