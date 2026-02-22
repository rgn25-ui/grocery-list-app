package com.grocerylist.app.ui.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.grocerylist.app.R;
import com.grocerylist.app.adapters.CategorySpinnerAdapter;
import com.grocerylist.app.adapters.GroceryItemSuggestionAdapter;
import com.grocerylist.app.models.Category;
import com.grocerylist.app.models.GroceryItemSuggestions;

import java.util.List;

/**
 * Helper class for item dialog setup
 * Eliminates duplicated code between AddItemDialogFragment and EditItemDialogFragment
 */
public class ItemDialogHelper {

    private static final int KEYBOARD_SHOW_DELAY_MS = 100;

    private ItemDialogHelper() {
        throw new AssertionError("ItemDialogHelper cannot be instantiated");
    }

    /**
     * Container for all dialog views
     */

    public static class ItemDialogViews {
        private EditText editTextName;
        private EditText editTextQuantity;
        private Spinner spinnerUnit;
        private EditText editTextNotes;
        private Spinner spinnerCategory;
        private ListView listViewSuggestions;
        private CheckBox checkBoxOnOffer;
        private LinearLayout layoutPrice;
        private EditText editTextPrice;

        public EditText getEditTextName() { return editTextName; }
        public void setEditTextName(EditText v) { editTextName = v; }

        public EditText getEditTextQuantity() { return editTextQuantity; }
        public void setEditTextQuantity(EditText v) { editTextQuantity = v; }

        public Spinner getSpinnerUnit() { return spinnerUnit; }
        public void setSpinnerUnit(Spinner v) { spinnerUnit = v; }

        public EditText getEditTextNotes() { return editTextNotes; }
        public void setEditTextNotes(EditText v) { editTextNotes = v; }

        public Spinner getSpinnerCategory() { return spinnerCategory; }
        public void setSpinnerCategory(Spinner v) { spinnerCategory = v; }

        public ListView getListViewSuggestions() { return listViewSuggestions; }
        public void setListViewSuggestions(ListView v) { listViewSuggestions = v; }

        public CheckBox getCheckBoxOnOffer() { return checkBoxOnOffer; }
        public void setCheckBoxOnOffer(CheckBox v) { checkBoxOnOffer = v; }

        public LinearLayout getLayoutPrice() { return layoutPrice; }
        public void setLayoutPrice(LinearLayout v) { layoutPrice = v; }

        public EditText getEditTextPrice() { return editTextPrice; }
        public void setEditTextPrice(EditText v) { editTextPrice = v; }
    }

    /**
     * Initialize all views from dialog layout
     */
    public static ItemDialogViews initializeViews(View dialogView) {
        ItemDialogViews views = new ItemDialogViews();

        views.setEditTextName(dialogView.findViewById(R.id.edit_text_name));
        views.setEditTextQuantity(dialogView.findViewById(R.id.edit_text_quantity));
        views.setSpinnerUnit(dialogView.findViewById(R.id.spinner_unit));
        views.setEditTextNotes(dialogView.findViewById(R.id.edit_text_notes));
        views.setSpinnerCategory(dialogView.findViewById(R.id.spinner_category));
        views.setListViewSuggestions(dialogView.findViewById(R.id.listview_suggestions));
        views.setCheckBoxOnOffer(dialogView.findViewById(R.id.checkbox_on_offer));
        views.setLayoutPrice(dialogView.findViewById(R.id.layout_price));
        views.setEditTextPrice(dialogView.findViewById(R.id.edit_text_price));

        return views;
    }

    /**
     * Setup spinners with adapters
     */
    public static void setupSpinners(Context context, ItemDialogViews views) {
        // Setup unit spinner
        String[] units = context.getResources().getStringArray(R.array.units_array);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        views.spinnerUnit.setAdapter(unitAdapter);
        views.spinnerUnit.setSelection(0); // "stk" is first

        // Setup category spinner
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(
                context,
                Category.values()
        );
        views.spinnerCategory.setAdapter(categoryAdapter);
        views.spinnerCategory.setSelection(Category.DIVERSE.ordinal());
    }

    /**
     * Setup autocomplete suggestions
     */
    public static GroceryItemSuggestionAdapter setupSuggestions(
            Context context,
            ItemDialogViews views,
            OnSuggestionSelectedListener listener) {

        GroceryItemSuggestionAdapter adapter = new GroceryItemSuggestionAdapter(context);
        views.listViewSuggestions.setAdapter(adapter);
        views.listViewSuggestions.setVisibility(View.GONE);

        // Click listener
        views.listViewSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            GroceryItemSuggestions.Suggestion suggestion = adapter.getItem(position);
            if (suggestion != null) {
                listener.onSuggestionSelected(suggestion);
            }
        });

        return adapter;
    }

    /**
     * Setup tilbud (offer) checkbox logic
     */
    public static void setupTilbudLogic(Context context, ItemDialogViews views) {
        views.checkBoxOnOffer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPriceInput(context, views);
            } else {
                hidePriceInput(context, views);
            }
        });
    }

    /**
     * Show price input with keyboard
     */
    private static void showPriceInput(Context context, ItemDialogViews views) {
        views.layoutPrice.setVisibility(View.VISIBLE);
        views.editTextPrice.requestFocus();

        views.editTextPrice.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(views.editTextPrice,
                        android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, KEYBOARD_SHOW_DELAY_MS);
    }

    /**
     * Hide price input and clear keyboard
     */
    private static void hidePriceInput(Context context, ItemDialogViews views) {
        views.layoutPrice.setVisibility(View.GONE);
        views.editTextPrice.setText("");

        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(views.editTextPrice.getWindowToken(), 0);
        }
    }

    /**
     * Handle suggestion query and update suggestions list
     */
    public static void handleSuggestionQuery(String query,
                                             GroceryItemSuggestionAdapter adapter,
                                             ListView listView) {
        if (query.length() >= 3) {
            List<GroceryItemSuggestions.Suggestion> suggestions =
                    GroceryItemSuggestions.getSuggestions(query, 5);

            if (!suggestions.isEmpty()) {
                adapter.updateSuggestions(suggestions);
                listView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.GONE);
            }
        } else {
            listView.setVisibility(View.GONE);
        }
    }

    /**
     * Validate item name input
     */
    public static boolean validateItemName(Context context, EditText editTextName) {
        String name = editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            editTextName.setError(context.getString(R.string.item_name_required));
            editTextName.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Callback interface for suggestion selection
     */
    public interface OnSuggestionSelectedListener {
        void onSuggestionSelected(GroceryItemSuggestions.Suggestion suggestion);
    }
}