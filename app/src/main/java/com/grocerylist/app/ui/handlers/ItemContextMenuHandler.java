package com.grocerylist.app.ui.handlers;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.utils.QuickItemsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles long-click context menu operations for grocery items
 * Manages edit, complete/incomplete, add to quick items, and delete actions
 */
public class ItemContextMenuHandler {

    private final Context context;
    private final QuickItemsManager quickItemsManager;
    private final OnItemActionListener actionListener;
    private final View snackbarAnchor;

    public interface OnItemActionListener {
        void onEditItem(GroceryItem groceryItem);
        void onToggleComplete(GroceryItem groceryItem);
        void onDeleteItem(GroceryItem groceryItem);
        void onQuickItemsChanged();
    }

    public ItemContextMenuHandler(Context context,
                                  QuickItemsManager quickItemsManager,
                                  OnItemActionListener actionListener,
                                  View snackbarAnchor) {
        this.context = context;
        this.quickItemsManager = quickItemsManager;
        this.actionListener = actionListener;
        this.snackbarAnchor = snackbarAnchor;
    }

    /**
     * Shows context menu for an item when long-pressed
     */
    public void showContextMenu(GroceryItem groceryItem) {
        boolean isQuickItem = quickItemsManager.isQuickItem(groceryItem.getName());

        List<String> options = buildMenuOptions(groceryItem, isQuickItem);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(groceryItem.getName())
                .setItems(options.toArray(new String[0]), (dialog, which) -> handleMenuSelection(groceryItem, which, isQuickItem));
        builder.show();
    }

    /**
     * Builds the list of menu options based on item state
     */
    private List<String> buildMenuOptions(GroceryItem groceryItem, boolean isQuickItem) {
        List<String> options = new ArrayList<>();

        // Always available options
        options.add(context.getString(R.string.edit)); // Index 0
        options.add(context.getString(
                groceryItem.isCompleted() ? R.string.mark_as_incomplete : R.string.mark_as_complete
        )); // Index 1

        // Conditional option: Add to quick items (only if not already a quick item)
        if (!isQuickItem) {
            options.add(context.getString(R.string.add_to_quick_items));
        }

        // Always at the end
        options.add(context.getString(R.string.delete)); // Last index

        return options;
    }

    /**
     * Handles the selected menu option
     */
    private void handleMenuSelection(GroceryItem groceryItem, int selectedIndex, boolean isQuickItem) {
        if (selectedIndex == 0) {
            // Edit
            actionListener.onEditItem(groceryItem);

        } else if (selectedIndex == 1) {
            // Toggle complete/incomplete
            actionListener.onToggleComplete(groceryItem);

        } else if (!isQuickItem && selectedIndex == 2) {
            // Add as quick groceryItem
            handleAddToQuickItems(groceryItem);

        } else {
            // Delete (always the last option)
            actionListener.onDeleteItem(groceryItem);
        }
    }

    /**
     * Handles adding an item to quick items list
     */
    private void handleAddToQuickItems(GroceryItem item) {
        if (quickItemsManager.addQuickItem(item.getName())) {
            actionListener.onQuickItemsChanged();
            showSnackbar(context.getString(R.string.added_to_quick_items, item.getName()));
        } else {
            showSnackbar(context.getString(R.string.max_quick_items_reached));
        }
    }

    /**
     * Shows a snackbar message
     */
    private void showSnackbar(String message) {
        Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_SHORT).show();
    }
}