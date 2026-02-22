package com.grocerylist.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.grocerylist.app.R;
import com.grocerylist.app.adapters.GroceryItemSuggestionAdapter;
import com.grocerylist.app.models.Category;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.ui.dialogs.ItemDialogHelper;
import com.grocerylist.app.utils.CategoryPredictor;
import com.grocerylist.app.utils.SimpleTextWatcher;

/**
 * Dialog for adding a new grocery item
 * Refactored to use ItemDialogHelper for common functionality
 */
public class AddItemDialogFragment extends DialogFragment {

    private ItemDialogHelper.ItemDialogViews views;
    private OnItemAddedListener listener;
    private GroceryItemSuggestionAdapter suggestionAdapter;

    public interface OnItemAddedListener {
        void onItemAdded(GroceryItem item);
    }

    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);

        views = ItemDialogHelper.initializeViews(dialogView);
        ItemDialogHelper.setupSpinners(requireContext(), views);
        setupSuggestions();
        ItemDialogHelper.setupTilbudLogic(requireContext(), views);
        setupCategoryPrediction();

        // Set default values
        views.getEditTextQuantity().setText("1");

        builder.setView(dialogView)
                .setTitle(getString(R.string.add_new_item))
                .setPositiveButton(getString(R.string.add_item), (dialog, which) -> {
                    if (ItemDialogHelper.validateItemName(requireContext(), views.getEditTextName())) {
                        createAndReturnItem();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null);

        return builder.create();
    }

    private void setupSuggestions() {
        suggestionAdapter = ItemDialogHelper.setupSuggestions(
                requireContext(),
                views,
                suggestion -> {
                    // Apply suggestion
                    views.getEditTextName().setText(suggestion.title);
                    views.getEditTextName().setSelection(suggestion.title.length());
                    views.getListViewSuggestions().setVisibility(View.GONE);

                    // Predict category
                    Category predicted = CategoryPredictor.predictCategory(suggestion.title);
                    views.getSpinnerCategory().setSelection(predicted.ordinal());
                }
        );

        // Text watcher for autocomplete
        views.getEditTextName().addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ItemDialogHelper.handleSuggestionQuery(
                        s.toString().trim(),
                        suggestionAdapter,
                        views.getListViewSuggestions()
                );
            }
        });
    }

    private void setupCategoryPrediction() {
        views.getEditTextName().addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String itemName = s.toString().trim();
                if (!itemName.isEmpty()) {
                    Category predicted = CategoryPredictor.predictCategory(itemName);
                    views.getSpinnerCategory().setSelection(predicted.ordinal());
                }
            }
        });
    }

    private void createAndReturnItem() {
        String name = views.getEditTextName().getText().toString().trim();
        String quantity = views.getEditTextQuantity().getText().toString().trim();
        String unit = views.getSpinnerUnit().getSelectedItem().toString();
        String notes = views.getEditTextNotes().getText().toString().trim();
        String price = views.getEditTextPrice().getText().toString().trim();
        boolean onOffer = views.getCheckBoxOnOffer().isChecked();

        if (quantity.isEmpty()) {
            quantity = "1";
        }

        Category selectedCategory = Category.values()[views.getSpinnerCategory().getSelectedItemPosition()];

        GroceryItem newItem = new GroceryItem();
        newItem.setName(name);
        newItem.setQuantity(quantity);
        newItem.setUnit(unit);
        newItem.setNotes(notes);
        newItem.setCategory(selectedCategory.name());
        newItem.setOnOffer(onOffer);
        newItem.setPrice(price);

        if (listener != null) {
            listener.onItemAdded(newItem);
        }
    }
}