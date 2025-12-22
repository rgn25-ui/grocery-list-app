package com.grocerylist.app.utils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.grocerylist.app.repository.GroceryRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages LiveData observers for item counts in RecyclerView
 * Prevents observer leaks and mixing when ViewHolders are recycled
 *
 * Problem this solves:
 * - RecyclerView reuses ViewHolders, so we must remove old observers before adding new ones
 * - Without proper cleanup, observers can pile up and display wrong data in wrong rows
 * - This helper tracks observers by list ID and ensures clean one-to-one relationships
 */
public class ItemCountObserverManager {

    private final GroceryRepository repository;
    private final LifecycleOwner lifecycleOwner;

    // Track active LiveData and observers by list ID to prevent mixing
    private final Map<String, LiveData<Integer>> activeLiveData = new HashMap<>();
    private final Map<String, Observer<Integer>> activeObservers = new HashMap<>();

    public ItemCountObserverManager(GroceryRepository repository, LifecycleOwner lifecycleOwner) {
        this.repository = repository;
        this.lifecycleOwner = lifecycleOwner;
    }

    /**
     * Observe item count for a specific list
     * Automatically removes any previous observer for this list ID
     *
     * @param listId The list to observe
     * @param callback Callback that receives the item count updates
     */
    public void observeItemCount(String listId, ItemCountCallback callback) {
        if (repository == null || lifecycleOwner == null) {
            callback.onItemCountLoaded(null);
            return;
        }

        // Remove previous observer for this list if it exists
        removeObserver(listId);

        // Create new observer for this list
        LiveData<Integer> itemCountLiveData = repository.getItemCountForList(listId);
        Observer<Integer> newObserver = itemCount -> callback.onItemCountLoaded(itemCount);

        // Store observer and LiveData for this list
        activeObservers.put(listId, newObserver);
        activeLiveData.put(listId, itemCountLiveData);

        // Start observing
        itemCountLiveData.observe(lifecycleOwner, newObserver);
    }

    /**
     * Remove observer for a specific list ID
     * Call this when ViewHolder is recycled or detached
     */
    public void removeObserver(String listId) {
        Observer<Integer> oldObserver = activeObservers.remove(listId);
        if (oldObserver != null) {
            LiveData<Integer> oldLiveData = activeLiveData.remove(listId);
            if (oldLiveData != null) {
                oldLiveData.removeObserver(oldObserver);
            }
        }
    }

    /**
     * Remove all observers
     * Call this when adapter is destroyed or cleared
     */
    public void removeAllObservers() {
        for (String listId : activeObservers.keySet()) {
            removeObserver(listId);
        }
        activeObservers.clear();
        activeLiveData.clear();
    }

    /**
     * Callback interface for item count updates
     */
    public interface ItemCountCallback {
        void onItemCountLoaded(Integer itemCount);
    }
}