package com.grocerylist.app.ui.handlers;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.grocerylist.app.adapters.GroceryItemAdapter;
import com.grocerylist.app.models.GroceryItem;

/**
 * Handles swipe gestures on grocery items
 * Left swipe: Toggle complete/incomplete
 * Right swipe: Delete with undo
 */
public class ItemSwipeHandler extends ItemTouchHelper.SimpleCallback {

    private final GroceryItemAdapter adapter;
    private final OnSwipeActionListener actionListener;
    private final View snackbarAnchor;
    private static final float SWIPE_DISTANCE_THRESHOLD = 0.2f;           // Distance needed to trigger swipe
    private static final float SWIPE_VELOCITY_MULTIPLIER = 0.3f; // Speed needed to trigger swipe
    private static final float SWIPE_ESCAPE_MULTIPLIER = 0.2f;   // Ease of completing swipe

    public interface OnSwipeActionListener {
        void onItemCompleteToggled(GroceryItem item);
        void onItemDeleted(String itemId);
        void onItemRestored(GroceryItem item);
    }

    public ItemSwipeHandler(GroceryItemAdapter adapter,
                            OnSwipeActionListener actionListener,
                            View snackbarAnchor) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.actionListener = actionListener;
        this.snackbarAnchor = snackbarAnchor;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        // Drag is disabled (first parameter is 0)
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        GroceryItem item = adapter.getItemAt(position);

        if (direction == ItemTouchHelper.LEFT) {
            handleLeftSwipe(item, position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            handleRightSwipe(item);
        }
    }

    /**
     * Handle left swipe - Toggle complete/incomplete
     */
    private void handleLeftSwipe(GroceryItem item, int position) {
        item.setCompleted(!item.isCompleted());
        actionListener.onItemCompleteToggled(item);
        adapter.notifyItemChanged(position);
    }

    /**
     * Handle right swipe - Delete with undo option
     */
    private void handleRightSwipe(GroceryItem item) {
        actionListener.onItemDeleted(item.getId());

        Snackbar.make(snackbarAnchor, "Item deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    item.setDeleted(false);
                    actionListener.onItemRestored(item);
                })
                .show();
    }

    // ===== SWIPE SENSITIVITY CONFIGURATION =====
    // Makes swiping easier for one-handed use

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_DISTANCE_THRESHOLD;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return defaultValue * SWIPE_VELOCITY_MULTIPLIER;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * SWIPE_ESCAPE_MULTIPLIER;
    }
}