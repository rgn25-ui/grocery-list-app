package com.grocerylist.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grocerylist.app.models.ListCategory;

/**
 * Adapter for displaying ListCategory enum in Spinners
 * Shows only store names (no badges) for cleaner dropdown appearance
 * Replaces inner class in MainActivity
 */
public class ListCategorySpinnerAdapter extends ArrayAdapter<ListCategory> {
    private final ListCategory[] categories;

    public ListCategorySpinnerAdapter(Context context, ListCategory[] categories) {
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

        ListCategory category = categories[position];
        TextView textView = convertView.findViewById(android.R.id.text1);

        if (textView != null) {
            // Only show store name (no badge in spinner)
            textView.setText(category.getDisplayName());

            // Add padding for dropdown items
            if (isDropdown) {
                textView.setPadding(32, 24, 32, 24);
                textView.setTextSize(16);
            }
        }

        return convertView;
    }
}