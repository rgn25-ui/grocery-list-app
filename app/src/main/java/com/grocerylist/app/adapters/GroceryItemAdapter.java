package com.grocerylist.app.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerylist.app.R;
import com.grocerylist.app.models.Category;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying grocery items in a RecyclerView
 * Handles sorting and provides interface for item interactions
 */
public class GroceryItemAdapter extends RecyclerView.Adapter<GroceryItemViewHolder> {

    private final List<GroceryItem> displayedItems = new ArrayList<>();
    private final List<GroceryItem> allItems = new ArrayList<>();

    // Sort types - using SortConstants
    public static final int SORT_BY_NAME = Constants.SORT_BY_NAME;
    public static final int SORT_BY_REMA1000 = Constants.SORT_BY_REMA1000;
    private int currentSortType = SORT_BY_REMA1000;

    // Listener interfaces
    private final OnItemClickListener clickListener;
    private final OnItemCompleteToggleListener completeToggleListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(GroceryItem item);
    }

    public interface OnItemCompleteToggleListener {
        void onItemCompleteToggle(GroceryItem item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(GroceryItem item);
    }

    // Constructor
    public GroceryItemAdapter(OnItemClickListener clickListener,
                              OnItemCompleteToggleListener completeToggleListener,
                              OnItemLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.completeToggleListener = completeToggleListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public GroceryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery_item, parent, false);

        // Create ViewHolder with listener that bridges to adapter's listeners
        return new GroceryItemViewHolder(view, new GroceryItemViewHolder.ItemInteractionListener() {
            @Override
            public void onItemClick(int position) {
                if (clickListener != null && position < displayedItems.size()) {
                    clickListener.onItemClick(displayedItems.get(position));
                }
            }

            @Override
            public void onItemLongClick(int position) {
                if (longClickListener != null && position < displayedItems.size()) {
                    longClickListener.onItemLongClick(displayedItems.get(position));
                }
            }

            @Override
            public void onCompleteToggle(int position) {
                if (completeToggleListener != null && position < displayedItems.size()) {
                    completeToggleListener.onItemCompleteToggle(displayedItems.get(position));
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryItemViewHolder holder, int position) {
        GroceryItem item = displayedItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    // ===== PUBLIC METHODS =====

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<GroceryItem> newItems) {
        allItems.clear();
        allItems.addAll(newItems);
        applySorting();
        notifyDataSetChanged();
    }

    public GroceryItem getItemAt(int position) {
        return displayedItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSortType(int sortType) {
        this.currentSortType = sortType;
        applySorting();
        notifyDataSetChanged();
    }

    // ===== SORTING LOGIC =====

    private void applySorting() {
        displayedItems.clear();
        displayedItems.addAll(allItems);

        switch (currentSortType) {
            case SORT_BY_NAME:
                sortByName();
                break;
            case SORT_BY_REMA1000:
            default:
                sortByRema1000();
        }
    }

    private void sortByRema1000() {
        displayedItems.sort((item1, item2) -> {
            // Completed items always go to bottom
            if (item1.isCompleted() && !item2.isCompleted()) return 1;
            if (!item1.isCompleted() && item2.isCompleted()) return -1;

            // Sort by Rema1000 store layout order
            Category cat1 = Category.getCategoryByName(item1.getCategory());
            Category cat2 = Category.getCategoryByName(item2.getCategory());

            int categoryCompare = Integer.compare(cat1.getSortOrder(), cat2.getSortOrder());
            if (categoryCompare != 0) return categoryCompare;

            // Same category → sort alphabetically by name
            return item1.getName().compareToIgnoreCase(item2.getName());
        });
    }

    private void sortByName() {
        displayedItems.sort((item1, item2) -> {
            // Completed items go to bottom
            if (item1.isCompleted() && !item2.isCompleted()) return 1;
            if (!item1.isCompleted() && item2.isCompleted()) return -1;

            // Sort by name alphabetically
            return item1.getName().compareToIgnoreCase(item2.getName());
        });
    }
}