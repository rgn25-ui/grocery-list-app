package com.grocerylist.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryItemSuggestions;

import java.util.List;

/**
 * Adapter for displaying grocery item autocomplete suggestions
 * Shows item title and category in a two-line layout
 * Replaces duplicated inner classes in AddItemDialogFragment and EditItemDialogFragment
 */
public class GroceryItemSuggestionAdapter extends ArrayAdapter<GroceryItemSuggestions.Suggestion> {

    public GroceryItemSuggestionAdapter(Context context) {
        super(context, 0);
    }

    /**
     * Update the adapter with new suggestions and refresh display
     */
    public void updateSuggestions(List<GroceryItemSuggestions.Suggestion> newSuggestions) {
        clear();
        addAll(newSuggestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_suggestion, parent, false);
        }

        GroceryItemSuggestions.Suggestion suggestion = getItem(position);
        if (suggestion != null) {
            TextView textTitle = convertView.findViewById(R.id.text_suggestion_title);
            TextView textCategory = convertView.findViewById(R.id.text_suggestion_category);

            textTitle.setText(suggestion.title);
            textCategory.setText(suggestion.categoryDisplay);
        }

        return convertView;
    }
}