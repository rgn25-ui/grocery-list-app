package com.grocerylist.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.SyncData;
import com.grocerylist.app.utils.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.HttpException;

/**
 * Manages synchronization between local and remote data sources.
 * Upload: every local change is marked pending and stays pending until the backend confirms it.
 * Uploads run in UploadWorker (WorkManager), so they complete even if the app is closed, and are
 * retried with backoff when the network or the backend is unavailable.
 * Download: cloud data is merged in with timestamp-based conflict resolution.
 */
public class SyncManager {
    private static final String TAG = "GrocerySync";
    private static final String PREF_LAST_SYNC_DURATION = "last_sync_duration";
    private static final long MIN_SYNC_INTERVAL_MS = 20000; // 20 seconds
    // Shared by all SyncManager instances (the app's and UploadWorker's), so uploads never run in parallel
    private static final Object UPLOAD_LOCK = new Object();
    private final Context appContext;
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SharedPreferences preferences;

    public interface OnSyncListener {
        void onSuccess();
        void onError(Exception error);
    }

    public SyncManager(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, Context context) {
        this.appContext = context.getApplicationContext();
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.preferences = context.getSharedPreferences(
                Constants.PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    // ===== UPLOAD OF PENDING CHANGES =====

    /** Schedules an upload of pending changes. Cheap to call after every local change. */
    public void requestUpload() {
        UploadWorker.enqueue(appContext);
    }

    /**
     * Uploads pending lists, then pending items, so an item's list exists in the backend first.
     * Stops at the first network error, rate limit or server error and leaves the rest pending.
     * Locked so the worker and a full sync never send the same changes in parallel.
     *
     * @return true if everything pending was handled, false if it stopped and should be retried
     */
    boolean uploadPendingChanges() {
        synchronized (UPLOAD_LOCK) {
            return uploadPendingChangesLocked();
        }
    }

    private boolean uploadPendingChangesLocked() {
        List<GroceryList> lists = localDataSource.getPendingLists();
        List<GroceryItem> items = localDataSource.getPendingItems();
        if (lists.isEmpty() && items.isEmpty()) {
            return true;
        }
        android.util.Log.d(TAG, "⬆️ Uploading " + lists.size() + " lists, " + items.size() + " items");

        for (GroceryList list : lists) {
            UploadPolicy.Outcome outcome = send(() -> remoteDataSource.createList(list).ignoreElement().blockingAwait(),
                    "list " + list.getName());
            if (outcome == UploadPolicy.Outcome.STOP) {
                return false;
            }
            if (outcome == UploadPolicy.Outcome.SENT) {
                localDataSource.markListSynced(list.getId(), list.getUpdatedAt());
            }
        }
        for (GroceryItem item : items) {
            UploadPolicy.Outcome outcome = send(() -> remoteDataSource.createItem(item).ignoreElement().blockingAwait(),
                    "item " + item.getName());
            if (outcome == UploadPolicy.Outcome.STOP) {
                return false;
            }
            if (outcome == UploadPolicy.Outcome.SENT) {
                localDataSource.markItemSynced(item.getId(), item.getUpdatedAt());
            }
        }
        android.util.Log.d(TAG, "✅ Upload completed");
        return true;
    }

    private UploadPolicy.Outcome send(Runnable call, String description) {
        try {
            call.run();
            return UploadPolicy.Outcome.SENT;
        } catch (RuntimeException e) {
            HttpException http = findHttpException(e);
            UploadPolicy.Outcome outcome = UploadPolicy.outcomeForFailure(http != null ? http.code() : null);
            android.util.Log.w(TAG, "⚠️ Upload of " + description + " failed - "
                    + (outcome == UploadPolicy.Outcome.SKIP ? "skipped, kept pending" : "stopping, will retry later"), e);
            return outcome;
        }
    }

    /** blockingAwait() wraps checked exceptions, so the HttpException may be a cause further down. */
    private static HttpException findHttpException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpException) {
                return (HttpException) current;
            }
            current = current.getCause();
        }
        return null;
    }

    // ===== SYNC OPERATIONS =====

    /**
     * Smart sync - only downloads if enough time has passed since last sync.
     * Pending changes are uploaded either way.
     */
    public void smartSync(String userId, OnSyncListener listener) {
        long lastSync = preferences.getLong(Constants.PREF_LAST_SYNC, 0);
        long timeSinceLastSync = System.currentTimeMillis() - lastSync;

        if (timeSinceLastSync < MIN_SYNC_INTERVAL_MS) {
            android.util.Log.d(TAG, "⏭️ Skipping download - synced " + timeSinceLastSync + "ms ago");
            requestUpload();
            listener.onSuccess();
            return;
        }

        forceFullSync(userId, listener);
    }

    /**
     * Full sync: uploads pending changes first, so the download cannot overwrite them,
     * then downloads and merges all data.
     */
    public void forceFullSync(String userId, OnSyncListener listener) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            android.util.Log.d(TAG, "🔄 Starting full sync...");
            try {
                if (!uploadPendingChanges()) {
                    // Leave the rest to the worker, which retries with backoff
                    requestUpload();
                }

                SyncData syncData = remoteDataSource.getAllData(userId).blockingGet();
                long networkTime = System.currentTimeMillis() - startTime;
                android.util.Log.d(TAG, "✅ Network calls completed in " + networkTime + "ms");
                android.util.Log.d(TAG, "📦 Received " +
                        (syncData.getLists() != null ? syncData.getLists().size() : 0) + " lists, " +
                        (syncData.getItems() != null ? syncData.getItems().size() : 0) + " items");

                long dbStartTime = System.currentTimeMillis();

                // Lists before items, so the items' lists exist locally
                if (syncData.getLists() != null && !syncData.getLists().isEmpty()) {
                    mergeListsFromCloud(syncData.getLists());
                }
                if (syncData.getItems() != null && !syncData.getItems().isEmpty()) {
                    mergeItemsFromCloud(syncData.getItems());
                }

                long totalTime = System.currentTimeMillis() - startTime;
                long dbTime = System.currentTimeMillis() - dbStartTime;

                preferences.edit()
                        .putLong(Constants.PREF_LAST_SYNC, System.currentTimeMillis())
                        .putLong(PREF_LAST_SYNC_DURATION, totalTime)
                        .apply();

                android.util.Log.d(TAG, "💾 Database save completed in " + dbTime + "ms");
                android.util.Log.d(TAG, "✅ Total sync time: " + totalTime + "ms");

                runOnMainThread(listener::onSuccess);

            } catch (Exception e) {
                long failTime = System.currentTimeMillis() - startTime;
                android.util.Log.e(TAG, "❌ Sync failed after " + failTime + "ms", e);
                runOnMainThread(() -> listener.onError(e));
            }
        }).start();
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
                localDataSource.insertList(cloudList);
                inserted++;
            } else if (cloudList.getUpdatedAt() > localList.getUpdatedAt()) {
                // Cloud version is newer, use it
                localDataSource.insertList(cloudList);
                updated++;
            } else {
                // Local version is newer or same age, keep it
                skipped++;
            }
        }

        android.util.Log.d(TAG, "📋 Lists: " + inserted + " inserted, " + updated + " updated, " + skipped + " skipped");
    }

    /**
     * Merges cloud items with local items using timestamp-based conflict resolution.
     * Items whose list does not exist locally are skipped: the backend also returns items of lists
     * deleted more than 30 days ago (without those lists), and inserting them would violate the
     * foreign key and fail the whole sync - e.g. right after the local database was recreated.
     */
    private void mergeItemsFromCloud(List<GroceryItem> cloudItems) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int withoutList = 0;
        Set<String> localListIds = new HashSet<>(localDataSource.getAllListIds());

        for (GroceryItem cloudItem : cloudItems) {
            if (!localListIds.contains(cloudItem.getListId())) {
                withoutList++;
                continue;
            }
            GroceryItem localItem = localDataSource.getItemById(cloudItem.getId());

            if (localItem == null) {
                localDataSource.insertItem(cloudItem);
                inserted++;
            } else if (cloudItem.getUpdatedAt() > localItem.getUpdatedAt()) {
                // Cloud version is newer, use it
                localDataSource.insertItem(cloudItem);
                updated++;
            } else {
                // Local version is newer or same age, keep it
                skipped++;
            }
        }

        android.util.Log.d(TAG, "🛒 Items: " + inserted + " inserted, " + updated + " updated, " + skipped
                + " skipped, " + withoutList + " without list");
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
