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
 * Dialog for editing an existing grocery item
 * Refactored to use ItemDialogHelper for common functionality
 */
public class EditItemDialogFragment extends DialogFragment {

    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_ITEM_QUANTITY = "item_quantity";
    private static final String ARG_ITEM_UNIT = "item_unit";
    private static final String ARG_ITEM_NOTES = "item_notes";
    private static final String ARG_ITEM_CATEGORY = "item_category";
    private static final String ARG_ITEM_ON_OFFER = "item_on_offer";
    private static final String ARG_ITEM_PRICE = "item_price";
    private static final String ARG_LIST_ID = "list_id";

    private ItemDialogHelper.ItemDialogViews views;
    private GroceryItem currentItem;
    private OnItemUpdatedListener listener;
    private GroceryItemSuggestionAdapter suggestionAdapter;

    public interface OnItemUpdatedListener {
        void onItemUpdated(GroceryItem item);
    }

    public static EditItemDialogFragment newInstance(GroceryItem item) {
        EditItemDialogFragment fragment = new EditItemDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, item.getId());
        args.putString(ARG_ITEM_NAME, item.getName());
        args.putString(ARG_ITEM_QUANTITY, item.getQuantity());
        args.putString(ARG_ITEM_UNIT, item.getUnit());
        args.putString(ARG_ITEM_NOTES, item.getNotes());
        args.putString(ARG_ITEM_CATEGORY, item.getCategory());
        args.putBoolean(ARG_ITEM_ON_OFFER, item.isOnOffer());
        args.putString(ARG_ITEM_PRICE, item.getPrice());
        args.putString(ARG_LIST_ID, item.getListId());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnItemUpdatedListener(OnItemUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentItem = createItemFromArguments();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);

        views = ItemDialogHelper.initializeViews(dialogView);
        ItemDialogHelper.setupSpinners(requireContext(), views);
        setupSuggestions();
        ItemDialogHelper.setupTilbudLogic(requireContext(), views);
        populateFields();

        builder.setView(dialogView)
                .setTitle(getString(R.string.edit_item))
                .setPositiveButton(getString(R.string.update), (dialog, which) -> {
                    if (ItemDialogHelper.validateItemName(requireContext(), views.getEditTextName())) {
                        updateAndReturnItem();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.delete), (dialog, which) -> {
                    if (currentItem != null) {
                        currentItem.setDeleted(true);
                        if (listener != null) {
                            listener.onItemUpdated(currentItem);
                        }
                    }
                });

        return builder.create();
    }

    private GroceryItem createItemFromArguments() {
        if (getArguments() == null) {
            return null;
        }

        GroceryItem item = new GroceryItem();
        item.setId(getArguments().getString(ARG_ITEM_ID, ""));
        item.setName(getArguments().getString(ARG_ITEM_NAME, ""));
        item.setQuantity(getArguments().getString(ARG_ITEM_QUANTITY, ""));
        item.setUnit(getArguments().getString(ARG_ITEM_UNIT, ""));
        item.setNotes(getArguments().getString(ARG_ITEM_NOTES, ""));
        item.setCategory(getArguments().getString(ARG_ITEM_CATEGORY, "DIVERSE"));
        item.setOnOffer(getArguments().getBoolean(ARG_ITEM_ON_OFFER, false));
        item.setPrice(getArguments().getString(ARG_ITEM_PRICE, ""));
        item.setListId(getArguments().getString(ARG_LIST_ID, ""));
        return item;
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

        // Focus listener to hide suggestions when focus lost
        views.getEditTextName().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                views.getListViewSuggestions().setVisibility(View.GONE);
            } else {
                String text = views.getEditTextName().getText().toString().trim();
                if (text.length() >= 3) {
                    ItemDialogHelper.handleSuggestionQuery(text, suggestionAdapter, views.getListViewSuggestions());
                }
            }
        });

        // Text watcher for autocomplete and category prediction
        views.getEditTextName().addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (views.getEditTextName().hasFocus()) {
                    ItemDialogHelper.handleSuggestionQuery(
                            s.toString().trim(),
                            suggestionAdapter,
                            views.getListViewSuggestions()
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    Category predicted = CategoryPredictor.predictCategory(query);
                    views.getSpinnerCategory().setSelection(predicted.ordinal());
                }
            }
        });
    }

    private void populateFields() {
        if (currentItem == null) {
            return;
        }

        views.getEditTextName().setText(currentItem.getName());
        views.getEditTextQuantity().setText(currentItem.getQuantity());
        views.getEditTextNotes().setText(currentItem.getNotes());
        views.getEditTextPrice().setText(currentItem.getPrice());
        views.getCheckBoxOnOffer().setChecked(currentItem.isOnOffer());
        views.getLayoutPrice().setVisibility(currentItem.isOnOffer() ? View.VISIBLE : View.GONE);

        // Set unit spinner selection
        String[] units = getResources().getStringArray(R.array.units_array);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(currentItem.getUnit())) {
                views.getSpinnerUnit().setSelection(i);
                break;
            }
        }

        // Set category spinner selection
        try {
            Category itemCategory = Category.valueOf(currentItem.getCategory());
            views.getSpinnerCategory().setSelection(itemCategory.ordinal());
        } catch (IllegalArgumentException e) {
            views.getSpinnerCategory().setSelection(Category.DIVERSE.ordinal());
        }
    }

    private void updateAndReturnItem() {
        if (currentItem == null) {
            return;
        }

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

        currentItem.setName(name);
        currentItem.setQuantity(quantity);
        currentItem.setUnit(unit);
        currentItem.setNotes(notes);
        currentItem.setCategory(selectedCategory.name());
        currentItem.setOnOffer(onOffer);
        currentItem.setPrice(price);
        currentItem.setUpdatedAt(System.currentTimeMillis());

        if (listener != null) {
            listener.onItemUpdated(currentItem);
        }
    }
}