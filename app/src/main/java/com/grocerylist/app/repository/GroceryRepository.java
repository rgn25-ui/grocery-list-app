package com.grocerylist.app.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.grocerylist.app.database.GroceryDatabase;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Main repository coordinating local and remote data sources
 * Delegates work to LocalDataSource, RemoteDataSource, and SyncManager
 */
public class GroceryRepository {
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SyncManager syncManager;
    private final String currentUserId;

    public interface Callback<T> {
        void onSuccess(@SuppressWarnings("unused") T result);
        void onError(Exception error);
    }

    public GroceryRepository(Application application) {
        // Initialize data sources
        GroceryDatabase database = GroceryDatabase.getDatabase(application);
        this.localDataSource = new LocalDataSource(database.groceryDao());
        this.remoteDataSource = new RemoteDataSource();
        this.syncManager = new SyncManager(localDataSource, remoteDataSource, application);

        // Set user ID
        this.currentUserId = "shared-user";
    }

    // ===== LOCAL DATA OPERATIONS (LiveData) =====

    public LiveData<List<GroceryList>> getAllLists() {
        return localDataSource.getAllLists();
    }

    public LiveData<List<GroceryItem>> getItemsForList(String listId) {
        return localDataSource.getItemsForList(listId);
    }

    public LiveData<Integer> getItemCountForList(String listId) {
        return localDataSource.getItemCountForList(listId);
    }

    // ===== LIST OPERATIONS =====

    public void insertList(GroceryList list, Callback<Void> callback) {
        new Thread(() -> {
            try {
                list.setUserId(currentUserId);
                localDataSource.insertList(list);
                syncListToCloud(list);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void deleteList(String listId, Callback<Void> callback) {
        new Thread(() -> {
            try {
                localDataSource.deleteList(listId, System.currentTimeMillis());
                deleteListFromCloud(listId);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void duplicateList(String originalListId, String newName, String category, Callback<String> callback) {
        new Thread(() -> {
            try {
                String newListId = localDataSource.duplicateList(originalListId, newName, category, currentUserId);

                // Sync new list to cloud
                GroceryList newList = localDataSource.getListById(newListId);
                syncListToCloud(newList);

                callback.onSuccess(newListId);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    // ===== ITEM OPERATIONS =====

    public void insertItem(GroceryItem item, Callback<Void> callback) {
        new Thread(() -> {
            try {
                localDataSource.insertItem(item);
                syncItemToCloud(item);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void updateItem(GroceryItem item, Callback<Void> callback) {
        new Thread(() -> {
            try {
                item.setUpdatedAt(System.currentTimeMillis());
                localDataSource.updateItem(item);
                syncItemToCloud(item);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void deleteItem(String itemId, Callback<Void> callback) {
        new Thread(() -> {
            try {
                localDataSource.deleteItem(itemId, System.currentTimeMillis());
                deleteItemFromCloud(itemId);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void clearCompletedItems(String listId, Callback<Void> callback) {
        new Thread(() -> {
            try {
                localDataSource.clearCompletedItems(listId);
                clearCompletedItemsFromCloud(listId);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    // ===== SYNC OPERATIONS =====

    public void smartSync(Callback<Void> callback) {
        syncManager.smartSync(currentUserId, new SyncManager.OnSyncListener() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    public void forceFullSync(Callback<Void> callback) {
        syncManager.forceFullSync(currentUserId, new SyncManager.OnSyncListener() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    public long getLastSyncTime() {
        return syncManager.getLastSyncTime();
    }

    public long getLastSyncDuration() {
        return syncManager.getLastSyncDuration();
    }

    // ===== CLOUD SYNC HELPERS =====

    private void syncListToCloud(GroceryList list) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.createList(list)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> android.util.Log.d("GroceryApp", "✅ List synced: " + result.getName()),
                                throwable -> android.util.Log.e("GroceryApp", "❌ Cloud sync failed", throwable)
                        )
        );
    }

    private void syncItemToCloud(GroceryItem item) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.createItem(item)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> { /* Successfully synced */ },
                                throwable -> { /* Handle sync error */ }
                        )
        );
    }

    private void deleteListFromCloud(String listId) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.deleteList(listId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> { /* Successfully deleted */ },
                                throwable -> { /* Handle delete error */ }
                        )
        );
    }

    private void deleteItemFromCloud(String itemId) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.deleteItem(itemId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> { /* Successfully deleted */ },
                                throwable -> { /* Handle delete error */ }
                        )
        );
    }

    private void clearCompletedItemsFromCloud(String listId) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.clearCompletedItems(listId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> { /* Successfully cleared */ },
                                throwable -> { /* Handle error */ }
                        )
        );
    }

    // ===== CLEAR ALL DATA =====

    public void clearAllData(Callback<Void> callback) {
        new Thread(() -> {
            try {
                // Clear local database
                localDataSource.deleteAllItems();
                localDataSource.deleteAllLists();

                // Clear cloud database
                clearCloudDatabase(callback);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private void clearCloudDatabase(Callback<Void> callback) {
        remoteDataSource.getDisposables().add(
                remoteDataSource.clearAllData(currentUserId, "CLEAR_GROCERY_DATA_2025")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> callback.onSuccess(null),
                                throwable -> callback.onSuccess(null) // Still call success since local was cleared
                        )
        );
    }

    // ===== CLEANUP =====

    public void cleanup() {
        remoteDataSource.cleanup();
    }
}