package com.grocerylist.app.ui.dialogs;

import android.content.Context;
import android.text.SpannableString;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.ListCategory;
import com.grocerylist.app.utils.DialogHelper;
import com.grocerylist.app.utils.SpannableBadgeHelper;

/**
 * Manages all list-related dialogs in MainActivity
 * Handles Add, Duplicate, Rename, and Delete confirmation dialogs
 */
public class ListDialogManager {

    private final Context context;
    private final OnListDialogListener listener;

    public ListDialogManager(Context context, OnListDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface OnListDialogListener {
        void onListCreated(String name, String category);
        void onListDuplicated(String originalListId, String newName, String category);
        void onListRenamed(GroceryList list, String newName, String newCategory);
        void onClearAllDataConfirmed();
    }

    // ===== ADD LIST DIALOG =====

    /**
     * Shows dialog to create a new grocery list
     */
    public void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.new_list_with_icon, context.getString(R.string.new_list)));

        DialogHelper.ListDialogComponents components = DialogHelper.createListDialogLayout(
                context,
                context.getString(R.string.hint_list_name),
                null,
                ListCategory.REMA
        );

        builder.setView(components.getLayout());

        builder.setPositiveButton(context.getString(R.string.create), (dialog, which) -> {
            String name = components.getName();
            if (name.isEmpty()) {
                name = context.getString(R.string.default_list_name);
            }
            listener.onListCreated(name, components.getSelectedCategory().name());
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    // ===== DUPLICATE LIST DIALOG =====

    /**
     * Shows dialog to duplicate an existing list
     */
    public void showDuplicateListDialog(GroceryList originalList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.duplicate_list));

        ListCategory initialCategory = ListCategory.getCategoryByName(originalList.getCategory());
        String initialName = originalList.getName() + context.getString(R.string.list_name_copy_suffix);

        DialogHelper.ListDialogComponents components = DialogHelper.createListDialogLayout(
                context,
                context.getString(R.string.hint_list_name),
                initialName,
                initialCategory
        );

        builder.setView(components.getLayout());

        builder.setPositiveButton(context.getString(R.string.duplicate), (dialog, which) -> {
            String name = components.getName();
            if (!name.isEmpty()) {
                listener.onListDuplicated(
                        originalList.getId(),
                        name,
                        components.getSelectedCategory().name()
                );
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    // ===== RENAME LIST DIALOG =====

    /**
     * Shows dialog to rename an existing list and change its category
     */
    public void showRenameListDialog(GroceryList list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Use SpannableBadgeHelper for title
        ListCategory listCategory = ListCategory.getCategoryByName(list.getCategory());
        SpannableString titleSpannable = SpannableBadgeHelper.createDialogTitleBadge(
                listCategory,
                context.getString(R.string.rename_list)
        );
        builder.setTitle(titleSpannable);

        ListCategory initialCategory = ListCategory.getCategoryByName(list.getCategory());

        DialogHelper.ListDialogComponents components = DialogHelper.createListDialogLayout(
                context,
                context.getString(R.string.hint_list_name),
                list.getName(),
                initialCategory
        );

        builder.setView(components.getLayout());

        builder.setPositiveButton(context.getString(R.string.rename), (dialog, which) -> {
            String name = components.getName();
            if (!name.isEmpty()) {
                listener.onListRenamed(
                        list,
                        name,
                        components.getSelectedCategory().name()
                );
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    // ===== CLEAR ALL DATA DIALOGS =====

    /**
     * Shows initial warning dialog for clearing all data
     */
    public void showClearAllDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.clear_all_data_title))
                .setMessage(context.getString(R.string.clear_all_data_message))
                .setPositiveButton(context.getString(R.string.clear_everything), (dialog, which) -> showFinalConfirmationDialog())
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFD32F2F);
    }

    /**
     * Shows final confirmation dialog requiring "SLET" input
     */
    private void showFinalConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.final_confirmation))
                .setMessage(context.getString(R.string.final_confirmation_message))
                .setIcon(android.R.drawable.ic_dialog_alert);

        EditText confirmInput = new EditText(context);
        confirmInput.setHint(context.getString(R.string.type_delete_to_confirm));
        builder.setView(confirmInput);

        builder.setPositiveButton(context.getString(R.string.delete_all_data), (dialog, which) -> {
            String confirmation = confirmInput.getText().toString().trim();
            if ("SLET".equals(confirmation)) {
                listener.onClearAllDataConfirmed();
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFD32F2F);
    }
}