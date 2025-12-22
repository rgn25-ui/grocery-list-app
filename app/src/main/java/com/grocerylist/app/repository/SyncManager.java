package com.grocerylist.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.utils.Constants;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Manages synchronization between local and remote data sources
 * Handles sync timing, conflict resolution, and merge logic
 */
public class SyncManager {
    private static final String PREF_LAST_SYNC_DURATION = "last_sync_duration";
    private static final long MIN_SYNC_INTERVAL_MS = 20000; // 20 seconds
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SharedPreferences preferences;

    public interface OnSyncListener {
        void onSuccess();
        void onError(Exception error);
    }

    public SyncManager(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, Context context) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.preferences = context.getSharedPreferences(
                Constants.PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    // ===== SYNC OPERATIONS =====

    /**
     * Smart sync - only syncs if enough time has passed since last sync
     */
    public void smartSync(String userId, OnSyncListener listener) {
        long lastSync = preferences.getLong(Constants.PREF_LAST_SYNC, 0);
        long timeSinceLastSync = System.currentTimeMillis() - lastSync;

        if (timeSinceLastSync < MIN_SYNC_INTERVAL_MS) {
            android.util.Log.d("GrocerySync", "⏭️ Skipping sync - synced " + timeSinceLastSync + "ms ago");
            listener.onSuccess();
            return;
        }

        forceFullSync(userId, listener);
    }

    /**
     * Force a full sync regardless of last sync time
     */
    public void forceFullSync(String userId, OnSyncListener listener) {
        long startTime = System.currentTimeMillis();
        android.util.Log.d("GrocerySync", "🔄 Starting full sync...");

        remoteDataSource.getDisposables().add(
                remoteDataSource.getAllData(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                syncData -> {
                                    long networkTime = System.currentTimeMillis() - startTime;
                                    android.util.Log.d("GrocerySync", "✅ Network call completed in " + networkTime + "ms");
                                    android.util.Log.d("GrocerySync", "📦 Received " +
                                            (syncData.getLists() != null ? syncData.getLists().size() : 0) + " lists, " +
                                            (syncData.getItems() != null ? syncData.getItems().size() : 0) + " items");

                                    // Perform database operations on background thread
                                    new Thread(() -> {
                                        long dbStartTime = System.currentTimeMillis();
                                        try {
                                            // Smart merge for lists
                                            if (syncData.getLists() != null && !syncData.getLists().isEmpty()) {
                                                mergeListsFromCloud(syncData.getLists());
                                            }

                                            // Smart merge for items
                                            if (syncData.getItems() != null && !syncData.getItems().isEmpty()) {
                                                mergeItemsFromCloud(syncData.getItems());
                                            }

                                            long totalTime = System.currentTimeMillis() - startTime;
                                            long dbTime = System.currentTimeMillis() - dbStartTime;

                                            // Save sync time and duration
                                            preferences.edit()
                                                    .putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis())
                                                    .putLong(PREF_LAST_SYNC_DURATION, totalTime)
                                                    .apply();

                                            android.util.Log.d("GrocerySync", "💾 Database save completed in " + dbTime + "ms");
                                            android.util.Log.d("GrocerySync", "✅ Total sync time: " + totalTime + "ms");

                                            // Call listener on main thread
                                            runOnMainThread(listener::onSuccess);

                                        } catch (Exception e) {
                                            android.util.Log.e("GrocerySync", "❌ Sync failed", e);
                                            runOnMainThread(() -> listener.onError(e));
                                        }
                                    }).start();
                                },
                                throwable -> {
                                    long failTime = System.currentTimeMillis() - startTime;
                                    android.util.Log.e("GrocerySync", "❌ Sync failed after " + failTime + "ms");
                                    listener.onError((Exception) throwable);
                                }
                        )
        );
    }

    // ===== MERGE LOGIC =====

    /**
     * Merges cloud lists with local lists using timestamp-based conflict resolution
     */
    private void mergeListsFromCloud(List<GroceryList> cloudLists) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (GroceryList cloudList : cloudLists) {
            GroceryList localList = localDataSource.getListById(cloudList.getId());

            if (localList == null) {
                // List doesn't exist locally, insert it
                localDataSource.insertList(cloudList);
                inserted++;
            } else {
                // List exists - keep the version with the most recent updatedAt timestamp
                if (cloudList.getUpdatedAt() > localList.getUpdatedAt()) {
                    // Cloud version is newer, use it
                    localDataSource.insertList(cloudList);
                    updated++;
                } else {
                    // Local version is newer or same age, keep it (do nothing)
                    skipped++;
                }
            }
        }

        android.util.Log.d("GrocerySync", "📋 Lists: " + inserted + " inserted, " + updated + " updated, " + skipped + " skipped");
    }

    /**
     * Merges cloud items with local items using timestamp-based conflict resolution
     */
    private void mergeItemsFromCloud(List<GroceryItem> cloudItems) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (GroceryItem cloudItem : cloudItems) {
            GroceryItem localItem = localDataSource.getItemById(cloudItem.getId());

            if (localItem == null) {
                // Item doesn't exist locally, insert it
                localDataSource.insertItem(cloudItem);
                inserted++;
            } else {
                // Item exists - keep the version with the most recent updatedAt timestamp
                if (cloudItem.getUpdatedAt() > localItem.getUpdatedAt()) {
                    // Cloud version is newer, use it
                    localDataSource.insertItem(cloudItem);
                    updated++;
                } else {
                    // Local version is newer or same age, keep it (do nothing)
                    skipped++;
                }
            }
        }

        android.util.Log.d("GrocerySync", "🛒 Items: " + inserted + " inserted, " + updated + " updated, " + skipped + " skipped");
    }

    // ===== HELPER METHODS =====

    private void runOnMainThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }

    public long getLastSyncTime() {
        return preferences.getLong(Constants.PREF_LAST_SYNC, 0);
    }

    public long getLastSyncDuration() {
        return preferences.getLong(PREF_LAST_SYNC_DURATION, 0);
    }
}