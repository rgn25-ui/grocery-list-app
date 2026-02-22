package com.grocerylist.app.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.grocerylist.app.R;
import com.grocerylist.app.adapters.ListCategorySpinnerAdapter;
import com.grocerylist.app.models.ListCategory;

/**
 * Helper class for creating standardized dialogs for list management
 * Eliminates duplicated dialog layout code in MainActivity
 */
public class DialogHelper {

    private DialogHelper() {
        throw new AssertionError("DialogHelper cannot be instantiated");
    }

    /**
     * Container class for dialog components
     * Provides convenient access to dialog inputs and selected values
     */
    public static class ListDialogComponents {
        LinearLayout layout;
        Spinner categorySpinner;
        EditText nameInput;

        public LinearLayout getLayout() { return layout; }

        /**
         * Get the currently selected list category from the spinner
         */
        public ListCategory getSelectedCategory() {
            return ListCategory.values()[categorySpinner.getSelectedItemPosition()];
        }

        /**
         * Get the trimmed name from the input field
         */
        public String getName() {
            return nameInput.getText().toString().trim();
        }
    }

    /**
     * Creates a standardized dialog layout with category spinner and name input
     * Used for Add, Duplicate, and Rename list dialogs
     *
     * @param context Android context
     * @param nameHint Hint text for the name input field
     * @param initialName Initial text for name field (null for empty)
     * @param initialCategory Initial category selection
     * @return ListDialogComponents containing the configured layout and inputs
     */
    public static ListDialogComponents createListDialogLayout(
            Context context,
            String nameHint,
            String initialName,
            ListCategory initialCategory) {

        ListDialogComponents components = new ListDialogComponents();

        // Create main layout
        components.layout = new LinearLayout(context);
        components.layout.setOrientation(LinearLayout.VERTICAL);
        components.layout.setPadding(50, 40, 50, 10);

        // Add category section
        addCategoryLabel(context, components.layout);
        components.categorySpinner = addCategorySpinner(context, components.layout, initialCategory);

        // Add name section
        addNameLabel(context, components.layout);
        components.nameInput = addNameInput(context, components.layout, nameHint, initialName);

        return components;
    }

    /**
     * Adds category label to the layout
     */
    private static void addCategoryLabel(Context context, LinearLayout layout) {
        TextView categoryLabel = new TextView(context);
        categoryLabel.setText(context.getString(R.string.store_label));
        categoryLabel.setTextSize(14);
        categoryLabel.setTypeface(null, Typeface.BOLD);
        categoryLabel.setPadding(0, 0, 0, 8);
        layout.addView(categoryLabel);
    }

    /**
     * Adds category spinner to the layout with proper styling
     */
    private static Spinner addCategorySpinner(Context context, LinearLayout layout, ListCategory initialCategory) {
        Spinner spinner = new Spinner(context);
        ListCategorySpinnerAdapter categoryAdapter = new ListCategorySpinnerAdapter(
                context,
                ListCategory.values()
        );
        spinner.setAdapter(categoryAdapter);
        spinner.setSelection(initialCategory.ordinal());

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 0, 0, 24);
        spinner.setLayoutParams(spinnerParams);
        layout.addView(spinner);

        return spinner;
    }

    /**
     * Adds name label to the layout
     */
    private static void addNameLabel(Context context, LinearLayout layout) {
        TextView nameLabel = new TextView(context);
        nameLabel.setText(context.getString(R.string.list_name_label));
        nameLabel.setTextSize(14);
        nameLabel.setTypeface(null, Typeface.BOLD);
        nameLabel.setPadding(0, 0, 0, 8);
        layout.addView(nameLabel);
    }

    /**
     * Adds name input field to the layout
     */
    private static EditText addNameInput(Context context, LinearLayout layout, String hint, String initialName) {
        EditText input = new EditText(context);
        input.setHint(hint);
        if (initialName != null && !initialName.isEmpty()) {
            input.setText(initialName);
        }
        layout.addView(input);

        return input;
    }
}