package com.grocerylist.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grocerylist.app.models.Category;

/**
 * Adapter for displaying Category enum in Spinners with emoji icons
 * Replaces duplicated inner classes in AddItemDialogFragment, EditItemDialogFragment
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    private final Category[] categories;

    public CategorySpinnerAdapter(Context context, Category[] categories) {
        super(context, 0, categories);
        this.categories = categories;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, true);
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean isDropdown) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        Category category = categories[position];
        TextView textView = convertView.findViewById(android.R.id.text1);

        if (textView != null) {
            // Display emoji + category name
            textView.setText(String.format("%s %s", category.getEmoji(), category.getDisplayName()));

            // Add padding for dropdown items
            if (isDropdown) {
                textView.setPadding(32, 24, 32, 24);
                textView.setTextSize(16);
            }
        }

        return convertView;
    }
}