package com.grocerylist.app.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.repository.GroceryRepository;

public class GroceryViewModel extends AndroidViewModel {
    private final GroceryRepository repository;

    // LiveData for UI observation
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    public GroceryViewModel(@NonNull Application application) {
        super(application);
        repository = new GroceryRepository(application);
    }

    // ===== GETTERS FOR UI OBSERVATION =====

    public LiveData<java.util.List<GroceryList>> getAllLists() {
        return repository.getAllLists();
    }

    public LiveData<java.util.List<GroceryItem>> getItemsForList(String listId) {
        return repository.getItemsForList(listId);
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public long getLastSyncTime() {
        return repository.getLastSyncTime();
    }

    public long getLastSyncDuration() {
        return repository.getLastSyncDuration();
    }

    public GroceryRepository getRepository() {
        return repository;
    }

    // ===== LIST OPERATIONS =====

    public void insertList(GroceryList list) {
        repository.insertList(list, createCallback("Failed to create list"));
    }

    public void updateList(GroceryList list) {
        repository.insertList(list, createCallback("Failed to update list"));
    }

    public void deleteList(String listId) {
        repository.deleteList(listId, createCallback("Failed to delete list"));
    }

    public void duplicateList(String originalListId, String newName, String category) {
        repository.duplicateList(originalListId, newName, category,
                createCallback("Failed to duplicate list"));
    }

    // ===== ITEM OPERATIONS =====

    public void insertItem(GroceryItem item) {
        repository.insertItem(item, createCallback("Failed to add item"));
    }

    public void updateItem(GroceryItem item) {
        repository.updateItem(item, createCallback("Failed to update item"));
    }

    public void deleteItem(String itemId) {
        repository.deleteItem(itemId, createCallback("Failed to delete item"));
    }

    public void clearCompletedItems(String listId) {
        repository.clearCompletedItems(listId, createCallback("Failed to clear completed items"));
    }

    // ===== SYNC OPERATIONS =====

    public void smartSync() {
        performSync(false);
    }

    public void forceFullSync() {
        performSync(true);
    }

    private void performSync(boolean forceFull) {
        isRefreshing.postValue(true);

        GroceryRepository.Callback<Void> callback = new GroceryRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isRefreshing.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue("Sync failed: " + e.getMessage());
                isRefreshing.postValue(false);
            }
        };

        if (forceFull) {
            repository.forceFullSync(callback);
        } else {
            repository.smartSync(callback);
        }
    }

    public String getLastSyncInfo() {
        long syncTime = getLastSyncTime();
        long duration = getLastSyncDuration();

        if (syncTime == 0) {
            return getString(R.string.never_synced);
        }

        String dateTime = com.grocerylist.app.utils.DateUtils.formatDisplayDateTime(syncTime);
        @SuppressLint("DefaultLocale")
        String durationStr = duration < 1000 ? duration + "ms" : String.format("%.1fs", duration / 1000.0);

        return getString(R.string.last_synced, dateTime, durationStr);
    }

    // ===== CLEAR ALL DATA =====

    public void clearAllData() {
        isRefreshing.postValue(true);
        syncStatus.postValue("Clearing all data...");

        repository.clearAllData(new GroceryRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                syncStatus.postValue("All data cleared successfully");
                isRefreshing.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue("Failed to clear data: " + e.getMessage());
                isRefreshing.postValue(false);
            }
        });
    }

    // ===== HELPER METHODS =====

    /**
     * Creates a standard callback that posts errors to LiveData
     */
    private <T> GroceryRepository.Callback<T> createCallback(String errorPrefix) {
        return new GroceryRepository.Callback<T>() {
            @Override
            public void onSuccess(T result) {
                // Success - silent by design
            }

            @Override
            public void onError(Exception e) {
                error.postValue(errorPrefix + ": " + e.getMessage());
            }
        };
    }

    private String getString(int resId) {
        return getApplication().getString(resId);
    }

    private String getString(int resId, Object... formatArgs) {
        return getApplication().getString(resId, formatArgs);
    }

    // ===== LIFECYCLE =====

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}