package com.grocerylist.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import java.util.List;

@Dao
public interface GroceryDao {
    @Query("SELECT * FROM grocery_lists WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    LiveData<List<GroceryList>> getAllLists();

    @Query("SELECT * FROM grocery_items WHERE listId = :listId AND isDeleted = 0 ORDER BY priority ASC, createdAt ASC")
    LiveData<List<GroceryItem>> getItemsForList(String listId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(GroceryList list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(GroceryItem item);

    @Update
    void updateItem(GroceryItem item);

    @Query("UPDATE grocery_lists SET isDeleted = 1, updatedAt = :timestamp, pendingSync = 1 WHERE id = :listId")
    void deleteList(String listId, long timestamp);

    @Query("UPDATE grocery_items SET isDeleted = 1, updatedAt = :timestamp, pendingSync = 1 WHERE id = :itemId")
    void deleteItem(String itemId, long timestamp);

    // Soft delete, so the deletion can be synced and the purchases stay in the analytics
    @Query("UPDATE grocery_items SET isDeleted = 1, updatedAt = :timestamp, pendingSync = 1 " +
           "WHERE listId = :listId AND isCompleted = 1 AND isDeleted = 0")
    void clearCompletedItems(String listId, long timestamp);

    @Query("SELECT * FROM grocery_lists WHERE id = :listId")
    GroceryList getListById(String listId);

    @Query("SELECT * FROM grocery_items WHERE listId = :listId AND isDeleted = 0")
    List<GroceryItem> getItemsForListSync(String listId);

    @Query("DELETE FROM grocery_items")
    void deleteAllItems();

    @Query("DELETE FROM grocery_lists")
    void deleteAllLists();

    @Query("SELECT COUNT(*) FROM grocery_items WHERE listId = :listId AND isDeleted = 0 AND isCompleted = 0")
    LiveData<Integer> getItemCountForListLive(String listId);

    @Query("SELECT * FROM grocery_items WHERE id = :itemId")
    GroceryItem getItemByIdSync(String itemId);

    @Query("SELECT id FROM grocery_lists")
    List<String> getAllListIds();

    // ===== PENDING SYNC =====

    @Query("SELECT * FROM grocery_lists WHERE pendingSync = 1 ORDER BY updatedAt ASC")
    List<GroceryList> getPendingLists();

    @Query("SELECT * FROM grocery_items WHERE pendingSync = 1 ORDER BY updatedAt ASC")
    List<GroceryItem> getPendingItems();

    // Only clears the flag if the row was not changed again while it was being uploaded
    @Query("UPDATE grocery_lists SET pendingSync = 0 WHERE id = :listId AND updatedAt = :updatedAt")
    void markListSynced(String listId, long updatedAt);

    @Query("UPDATE grocery_items SET pendingSync = 0 WHERE id = :itemId AND updatedAt = :updatedAt")
    void markItemSynced(String itemId, long updatedAt);

    // Number of local changes not yet confirmed by the backend (for the status line)
    @Query("SELECT (SELECT COUNT(*) FROM grocery_lists WHERE pendingSync = 1) " +
           "+ (SELECT COUNT(*) FROM grocery_items WHERE pendingSync = 1)")
    LiveData<Integer> getPendingChangeCount();


}