package com.grocerylist.app.adapters;

import android.graphics.Paint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerylist.app.R;
import com.grocerylist.app.models.Category;
import com.grocerylist.app.models.GroceryItem;

/**
 * ViewHolder for displaying individual grocery items
 * Handles all item display logic including completion state, categories, and offers
 */
public class GroceryItemViewHolder extends RecyclerView.ViewHolder {

    // UI Components
    private final CheckBox checkBoxCompleted;
    private final TextView textName;
    private final TextView textQuantityUnit;
    private final TextView textNotes;
    private final ImageView imageCategoryIcon;
    private final View categoryIndicator;
    private final LinearLayout layoutOfferIndicator;
    private final TextView textOfferPrice;
    private final TextView textCategoryBadge;
    private final TextView textTilbudLabel;

    // Listeners
    private final ItemInteractionListener listener;

    public interface ItemInteractionListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onCompleteToggle(int position);
    }

    public GroceryItemViewHolder(@NonNull View itemView, ItemInteractionListener listener) {
        super(itemView);
        this.listener = listener;

        // Initialize views
        checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
        textName = itemView.findViewById(R.id.text_name);
        textQuantityUnit = itemView.findViewById(R.id.text_quantity_unit);
        textNotes = itemView.findViewById(R.id.text_notes);
        imageCategoryIcon = itemView.findViewById(R.id.image_category_icon);
        categoryIndicator = itemView.findViewById(R.id.category_indicator);
        layoutOfferIndicator = itemView.findViewById(R.id.layout_offer_indicator);
        textOfferPrice = itemView.findViewById(R.id.text_offer_price);
        textCategoryBadge = itemView.findViewById(R.id.text_category_badge);
        textTilbudLabel = itemView.findViewById(R.id.text_tilbud_label);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Item click (edit item)
        itemView.setOnClickListener(view -> {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(position);
            }
        });

        // Long click for context menu
        itemView.setOnLongClickListener(view -> {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemLongClick(position);
                return true;
            }
            return false;
        });

        // Checkbox click (toggle completion)
        checkBoxCompleted.setOnClickListener(view -> {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onCompleteToggle(position);
            }
        });
    }

    // ===== MAIN BIND METHOD =====

    public void bind(GroceryItem item) {
        setupCheckbox(item);
        setupItemText(item);
        setupQuantityUnit(item);
        setupNotes(item);
        setupCategory(item);
        setupOfferIndicator(item);
        setupItemAppearance(item);
    }

    // ===== INDIVIDUAL SETUP METHODS =====

    private void setupCheckbox(GroceryItem item) {
        checkBoxCompleted.setChecked(item.isCompleted());
    }

    private void setupItemText(GroceryItem item) {
        textName.setText(item.getName());

        if (item.isCompleted()) {
            textName.setPaintFlags(textName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.item_completed_text));
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.item_completed_bg));
        } else {
            textName.setPaintFlags(textName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.item_pending_text));
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.item_pending_bg));
        }
    }

    private void setupQuantityUnit(GroceryItem item) {
        String quantityUnit = buildQuantityUnitString(item);
        if (!quantityUnit.isEmpty()) {
            textQuantityUnit.setText(quantityUnit);
            textQuantityUnit.setVisibility(View.VISIBLE);
        } else {
            textQuantityUnit.setVisibility(View.GONE);
        }
    }

    private void setupNotes(GroceryItem item) {
        if (!item.getNotes().isEmpty()) {
            textNotes.setText(item.getNotes());
            textNotes.setVisibility(View.VISIBLE);
        } else {
            textNotes.setVisibility(View.GONE);
        }
    }

    private void setupCategory(GroceryItem item) {
        try {
            Category category = Category.getCategoryByName(item.getCategory());
            applyCategoryVisuals(category);
        } catch (IllegalArgumentException e) {
            applyCategoryVisuals(Category.DIVERSE);
        }
    }

    private void applyCategoryVisuals(Category category) {
        // Wide color indicator (8dp)
        if (categoryIndicator != null) {
            categoryIndicator.setBackgroundColor(category.getColor());
        }

        // Category icon with color
        if (imageCategoryIcon != null) {
            imageCategoryIcon.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            imageCategoryIcon.setColorFilter(category.getColor());
        }

        // Category name badge
        if (textCategoryBadge != null) {
            textCategoryBadge.setText(category.getDisplayName().toUpperCase());
            textCategoryBadge.setBackgroundColor(category.getColor());
            textCategoryBadge.setVisibility(View.VISIBLE);
        }
    }

    private void setupOfferIndicator(GroceryItem item) {
        if (layoutOfferIndicator == null) {
            return;
        }

        if (!item.isOnOffer()) {
            layoutOfferIndicator.setVisibility(View.GONE);
            return;
        }

        // Show offer indicator
        layoutOfferIndicator.setVisibility(View.VISIBLE);

        // Always show TILBUD label when on offer
        if (textTilbudLabel != null) {
            textTilbudLabel.setText(R.string.tilbud_label);
            textTilbudLabel.setVisibility(View.VISIBLE);
        }

        // Show price only if it exists
        setupOfferPrice(item);

        // Dim indicator if item is completed
        float alpha = item.isCompleted() ? 0.5f : 1.0f;
        layoutOfferIndicator.setAlpha(alpha);
    }

    private void setupOfferPrice(GroceryItem item) {
        if (textOfferPrice == null) {
            return;
        }

        if (item.getPrice().isEmpty()) {
            textOfferPrice.setVisibility(View.GONE);
            return;
        }

        // Format and show price
        String priceText = item.getPrice();
        if (!priceText.contains("kr")) {
            priceText = priceText + " kr";
        }
        textOfferPrice.setText(priceText);
        textOfferPrice.setVisibility(View.VISIBLE);
    }

    private void setupItemAppearance(GroceryItem item) {
        float alpha = item.isCompleted() ? 0.6f : 1.0f;
        itemView.setAlpha(alpha);
    }

    // ===== HELPER METHODS =====

    private String buildQuantityUnitString(GroceryItem item) {
        String quantity = item.getQuantity();
        String unit = item.getUnit();

        if (quantity.isEmpty() && unit.isEmpty()) {
            return "";
        } else if (quantity.isEmpty()) {
            return unit;
        } else if (unit.isEmpty()) {
            return quantity;
        } else {
            return quantity + " " + unit;
        }
    }
}