package com.grocerylist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.grocerylist.app.R;
import java.util.List;

public class QuickItemAdapter extends RecyclerView.Adapter<QuickItemAdapter.ViewHolder> {

    private final List<String> quickItems;
    private final OnQuickItemClickListener clickListener;
    private final OnQuickItemLongClickListener longClickListener;

    public interface OnQuickItemClickListener {
        void onQuickItemClick(String itemName);
    }

    public interface OnQuickItemLongClickListener {
        void onQuickItemLongClick(String itemName);
    }

    public QuickItemAdapter(List<String> quickItems,
                            OnQuickItemClickListener clickListener,
                            OnQuickItemLongClickListener longClickListener) {
        this.quickItems = quickItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_add_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String itemName = quickItems.get(position);
        holder.bind(itemName);
    }

    @Override
    public int getItemCount() {
        return quickItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Button chipQuickItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            chipQuickItem = itemView.findViewById(R.id.chip_quick_item);
        }

        void bind(String itemName) {
            chipQuickItem.setText(itemName);

            // Normal click - add item to list
            chipQuickItem.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onQuickItemClick(itemName);
                }
            });

            // Long click - remove from quick items
            chipQuickItem.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onQuickItemLongClick(itemName);
                    return true;
                }
                return false;
            });
        }
    }
}