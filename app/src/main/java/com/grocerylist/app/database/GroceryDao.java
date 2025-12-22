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

    @Query("UPDATE grocery_lists SET isDeleted = 1, updatedAt = :timestamp WHERE id = :listId")
    void deleteList(String listId, long timestamp);

    @Query("UPDATE grocery_items SET isDeleted = 1, updatedAt = :timestamp WHERE id = :itemId")
    void deleteItem(String itemId, long timestamp);

    @Query("DELETE FROM grocery_items WHERE listId = :listId AND isCompleted = 1")
    void clearCompletedItems(String listId);

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


}