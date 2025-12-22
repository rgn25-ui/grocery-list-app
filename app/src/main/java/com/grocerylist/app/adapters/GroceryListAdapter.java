package com.grocerylist.app.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.ListCategory;
import com.grocerylist.app.repository.GroceryRepository;
import com.grocerylist.app.utils.DateUtils;
import com.grocerylist.app.utils.ItemCountObserverManager;
import com.grocerylist.app.utils.SpannableBadgeHelper;

import java.util.ArrayList;
import java.util.List;

import android.text.SpannableString;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.ViewHolder> {
    private final List<GroceryList> lists = new ArrayList<>();
    private final OnListClickListener clickListener;
    private final OnListLongClickListener longClickListener;
    private final ItemCountObserverManager observerManager;

    public interface OnListClickListener {
        void onListClick(GroceryList list);
    }

    public interface OnListLongClickListener {
        void onListLongClick(GroceryList list, View anchorView);
    }

    public GroceryListAdapter(OnListClickListener clickListener,
                              OnListLongClickListener longClickListener,
                              GroceryRepository repository,
                              LifecycleOwner lifecycleOwner) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.observerManager = new ItemCountObserverManager(repository, lifecycleOwner);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroceryList list = lists.get(position);
        holder.bind(list);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<GroceryList> newLists) {
        lists.clear();
        lists.addAll(newLists);
        notifyDataSetChanged();
    }

    public GroceryList getListAt(int position) {
        return lists.get(position);
    }

    /**
     * Clean up observers when adapter is destroyed
     */
    public void cleanup() {
        observerManager.removeAllObservers();
    }

    // ===== VIEW HOLDER =====

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textListName;
        private final TextView textItemCount;
        private final TextView textUpdated;

        ViewHolder(View itemView) {
            super(itemView);

            textListName = itemView.findViewById(R.id.text_list_name);
            textItemCount = itemView.findViewById(R.id.text_item_count);
            textUpdated = itemView.findViewById(R.id.text_updated);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onListClick(lists.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    longClickListener.onListLongClick(lists.get(position), itemView);
                    return true;
                }
                return false;
            });
        }

        void bind(GroceryList list) {
            ListCategory listCategory = ListCategory.getCategoryByName(list.getCategory());

            SpannableString spannable = SpannableBadgeHelper.createListCategoryBadge(
                    listCategory,
                    list.getName()
            );

            textListName.setText(spannable);
            textUpdated.setText(DateUtils.getRelativeTimeString(list.getUpdatedAt()));

            setupItemCountDisplay(list);
        }

        private void setupItemCountDisplay(GroceryList list) {
            String listId = list.getId();

            // Show loading state immediately
            textItemCount.setText(itemView.getContext().getString(R.string.loading_items));

            // Use observer manager to handle LiveData observation
            observerManager.observeItemCount(listId, itemCount -> {
                // Verify this ViewHolder is still showing the same list
                int currentPosition = getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION &&
                        currentPosition < lists.size() &&
                        lists.get(currentPosition).getId().equals(listId)) {

                    updateItemCountText(itemCount);
                }
            });
        }

        private void updateItemCountText(Integer itemCount) {
            if (itemCount != null) {
                if (itemCount == 0) {
                    textItemCount.setText(itemView.getContext().getString(R.string.no_items_remaining));
                } else if (itemCount == 1) {
                    textItemCount.setText(itemView.getContext().getString(R.string.one_item_remaining));
                } else {
                    textItemCount.setText(itemView.getContext().getString(R.string.multiple_items_remaining, itemCount));
                }
            } else {
                textItemCount.setText(itemView.getContext().getString(R.string.no_items));
            }
        }
    }
}