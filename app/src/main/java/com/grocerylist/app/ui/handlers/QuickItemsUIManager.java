package com.grocerylist.app.ui.handlers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.grocerylist.app.R;
import com.grocerylist.app.adapters.QuickItemAdapter;
import com.grocerylist.app.models.Category;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.utils.CategoryPredictor;
import com.grocerylist.app.utils.QuickItemsManager;

import java.util.List;

/**
 * Manages the Quick Items UI section
 * Handles expanding/collapsing, adding items from quick list, and managing quick items
 */
public class QuickItemsUIManager {

    private static final int GRID_COLUMNS = 3;  // Number of columns in quick items grid
    private static final int ANIMATION_DURATION_MS = 300;  // Expand/collapse animation duration
    private static final String DEFAULT_QUANTITY = "1";
    private static final String DEFAULT_UNIT = "stk";
    private final Context context;
    private final QuickItemsManager quickItemsManager;
    private final RecyclerView recyclerViewQuickItems;
    private final Button btnToggleQuickItems;
    private final LinearLayout layoutQuickItemsContainer;
    private final View snackbarAnchor;
    private final OnQuickItemActionListener actionListener;

    private QuickItemAdapter quickItemAdapter;
    private boolean isQuickItemsExpanded = false;

    public interface OnQuickItemActionListener {
        void onQuickItemClicked(GroceryItem newItem);
    }

    public QuickItemsUIManager(Context context,
                               QuickItemsManager quickItemsManager,
                               RecyclerView recyclerViewQuickItems,
                               Button btnToggleQuickItems,
                               LinearLayout layoutQuickItemsContainer,
                               View snackbarAnchor,
                               OnQuickItemActionListener actionListener) {
        this.context = context;
        this.quickItemsManager = quickItemsManager;
        this.recyclerViewQuickItems = recyclerViewQuickItems;
        this.btnToggleQuickItems = btnToggleQuickItems;
        this.layoutQuickItemsContainer = layoutQuickItemsContainer;
        this.snackbarAnchor = snackbarAnchor;
        this.actionListener = actionListener;
    }

    /**
     * Initialize the quick items UI
     */
    public void setup(String listId) {
        List<String> quickItems = quickItemsManager.getQuickItems();

        quickItemAdapter = new QuickItemAdapter(
                quickItems,
                itemName -> handleQuickItemClick(itemName, listId),
                this::handleQuickItemLongClick
        );

        // Use GridLayoutManager for better space utilization
        GridLayoutManager layoutManager = new GridLayoutManager(context, GRID_COLUMNS);
        recyclerViewQuickItems.setLayoutManager(layoutManager);
        recyclerViewQuickItems.setAdapter(quickItemAdapter);

        // Setup toggle button
        btnToggleQuickItems.setOnClickListener(v -> toggleQuickItems());
    }

    /**
     * Toggle expand/collapse of quick items section
     */
    private void toggleQuickItems() {
        if (isQuickItemsExpanded) {
            collapseQuickItems();
        } else {
            expandQuickItems();
        }
        isQuickItemsExpanded = !isQuickItemsExpanded;
    }

    /**
     * Expand the quick items section with animation
     */
    private void expandQuickItems() {
        layoutQuickItemsContainer.measure(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        final int targetHeight = layoutQuickItemsContainer.getMeasuredHeight();

        layoutQuickItemsContainer.getLayoutParams().height = 0;
        layoutQuickItemsContainer.setVisibility(View.VISIBLE);

        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            layoutQuickItemsContainer.getLayoutParams().height = (int) animation.getAnimatedValue();
            layoutQuickItemsContainer.requestLayout();
        });
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.start();

        btnToggleQuickItems.setText(context.getString(R.string.quick_items_expanded));
        btnToggleQuickItems.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, android.R.drawable.arrow_up_float, 0
        );
    }

    /**
     * Collapse the quick items section with animation
     */
    private void collapseQuickItems() {
        final int initialHeight = layoutQuickItemsContainer.getMeasuredHeight();

        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            layoutQuickItemsContainer.getLayoutParams().height = (int) animation.getAnimatedValue();
            layoutQuickItemsContainer.requestLayout();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                layoutQuickItemsContainer.setVisibility(View.GONE);
            }
        });
        animator.setDuration(300);
        animator.start();

        btnToggleQuickItems.setText(context.getString(R.string.quick_items_collapsed));
        btnToggleQuickItems.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, android.R.drawable.arrow_down_float, 0
        );
    }

    /**
     * Handle click on a quick item - adds it to the list
     */
    private void handleQuickItemClick(String itemName, String listId) {
        // Create a new item with predicted category
        GroceryItem newItem = new GroceryItem(listId, itemName);
        Category predictedCategory = CategoryPredictor.predictCategory(itemName);
        newItem.setCategory(predictedCategory.name());
        newItem.setQuantity(DEFAULT_QUANTITY);
        newItem.setUnit(DEFAULT_UNIT);

        // Notify listener
        actionListener.onQuickItemClicked(newItem);

        // Show confirmation
        Snackbar.make(snackbarAnchor,
                context.getString(R.string.item_added, itemName),
                Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Handle long click on a quick item - removes it from quick items
     */
    private void handleQuickItemLongClick(String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.remove_quick_item_title))
                .setMessage(context.getString(R.string.remove_quick_item_message, itemName))
                .setPositiveButton(context.getString(R.string.remove), (dialog, which) -> {
                    quickItemsManager.removeQuickItem(itemName);
                    refreshQuickItems();
                    Snackbar.make(snackbarAnchor,
                            context.getString(R.string.removed_from_quick_items, itemName),
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    /**
     * Refresh the quick items list
     */
    public void refreshQuickItems() {
        List<String> quickItems = quickItemsManager.getQuickItems();
        quickItemAdapter = new QuickItemAdapter(
                quickItems,
                itemName -> handleQuickItemClick(itemName, ""), // listId will be set by caller
                this::handleQuickItemLongClick
        );
        recyclerViewQuickItems.setAdapter(quickItemAdapter);
    }

    /**
     * Show dialog to reset quick items to defaults
     */
    public void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.reset_quick_items_title))
                .setMessage(context.getString(R.string.reset_quick_items_message))
                .setPositiveButton(context.getString(R.string.reset), (dialog, which) -> {
                    quickItemsManager.resetToDefaults();
                    refreshQuickItems();
                    Snackbar.make(snackbarAnchor,
                            context.getString(R.string.quick_items_reset),
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }
}