package com.grocerylist.app;

import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.grocerylist.app.adapters.GroceryItemAdapter;
import com.grocerylist.app.fragments.AddItemDialogFragment;
import com.grocerylist.app.fragments.EditItemDialogFragment;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.ListCategory;
import com.grocerylist.app.ui.handlers.ItemContextMenuHandler;
import com.grocerylist.app.ui.handlers.ItemSwipeHandler;
import com.grocerylist.app.ui.handlers.QuickItemsUIManager;
import com.grocerylist.app.utils.QuickItemsManager;
import com.grocerylist.app.utils.SpannableBadgeHelper;
import com.grocerylist.app.viewmodel.GroceryViewModel;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Activity for displaying and managing items within a grocery list
 * Refactored to use handler classes for better separation of concerns
 */
public class ListDetailActivity extends AppCompatActivity {

    private GroceryItemAdapter adapter;
    private GroceryViewModel viewModel;
    private String currentListId;
    private String currentListName;
    private String currentListCategory;

    // Views
    private RecyclerView recyclerViewItems;
    private View emptyView;
    private FloatingActionButton fabAddItem;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textSyncInfo;
    private LoadingMessageManager loadingMessageManager;

    // Handlers
    private ItemContextMenuHandler contextMenuHandler;
    private QuickItemsUIManager quickItemsManager;
    private static final int SYNC_FALLBACK_TIMEOUT_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        currentListId = getIntent().getStringExtra("list_id");
        currentListName = getIntent().getStringExtra("list_name");
        currentListCategory = getIntent().getStringExtra("list_category");

        setupViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupHandlers();
        setupQuickItems();
        setupFab();
        setupSwipeRefresh();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewModel.smartSync();
    }

    private void setupViews() {
        recyclerViewItems = findViewById(R.id.recycler_view_items);
        emptyView = findViewById(R.id.empty_view);
        fabAddItem = findViewById(R.id.fab_add_item);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        textSyncInfo = findViewById(R.id.text_sync_info);

        // Setup loading messages
        View loadingOverlay = findViewById(R.id.loading_overlay);
        TextView loadingMessage = findViewById(R.id.loading_message);
        loadingMessageManager = new LoadingMessageManager(loadingOverlay, loadingMessage);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            ListCategory category = ListCategory.getCategoryByName(currentListCategory);
            String displayName = currentListName != null ? currentListName : getString(R.string.app_name);
            SpannableString spannable = SpannableBadgeHelper.createListCategoryBadge(category, displayName);

            getSupportActionBar().setTitle(spannable);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GroceryViewModel.class);

        // Observe items for this list
        viewModel.getItemsForList(currentListId).observe(this, items -> {
            adapter.submitList(items);
            emptyView.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(recyclerViewItems, error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe sync status
        viewModel.getSyncStatus().observe(this, status -> {
            if (status != null) {
                Snackbar.make(recyclerViewItems, status, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsRefreshing().observe(this, isRefreshing -> {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(isRefreshing != null ? isRefreshing : false);
            }

            if (isRefreshing != null && isRefreshing) {
                loadingMessageManager.show();
            } else {
                loadingMessageManager.hide();
            }

            if (isRefreshing != null && !isRefreshing) {
                updateSyncInfo();
            }
        });

        updateSyncInfo();
    }

    private void setupRecyclerView() {
        adapter = new GroceryItemAdapter(
                this::onItemClick,
                this::onItemCompleteToggle,
                this::onItemLongClick
        );

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupHandlers() {
        // Setup context menu handler
        contextMenuHandler = new ItemContextMenuHandler(
                this,
                new QuickItemsManager(this),
                new ItemContextMenuHandler.OnItemActionListener() {
                    @Override
                    public void onEditItem(GroceryItem groceryItem) {
                        showEditItemDialog(groceryItem);
                    }

                    @Override
                    public void onToggleComplete(GroceryItem groceryItem) {
                        onItemCompleteToggle(groceryItem);
                    }

                    @Override
                    public void onDeleteItem(GroceryItem groceryItem) {
                        viewModel.deleteItem(groceryItem.getId());
                    }

                    @Override
                    public void onQuickItemsChanged() {
                        if (quickItemsManager != null) {
                            quickItemsManager.refreshQuickItems();
                        }
                    }
                },
                recyclerViewItems
        );

        // Setup swipe handler
        ItemSwipeHandler swipeHandler = new ItemSwipeHandler(
                adapter,
                new ItemSwipeHandler.OnSwipeActionListener() {
                    @Override
                    public void onItemCompleteToggled(GroceryItem item) {
                        viewModel.updateItem(item);
                    }

                    @Override
                    public void onItemDeleted(String itemId) {
                        viewModel.deleteItem(itemId);
                    }

                    @Override
                    public void onItemRestored(GroceryItem item) {
                        viewModel.insertItem(item);
                    }
                },
                recyclerViewItems
        );

        new ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerViewItems);
    }

    private void setupQuickItems() {
        RecyclerView recyclerViewQuickItems = findViewById(R.id.recycler_view_quick_items);
        Button btnToggleQuickItems = findViewById(R.id.btn_toggle_quick_items);
        LinearLayout layoutQuickItemsContainer = findViewById(R.id.layout_quick_items_container);

        quickItemsManager = new QuickItemsUIManager(
                this,
                new QuickItemsManager(this),
                recyclerViewQuickItems,
                btnToggleQuickItems,
                layoutQuickItemsContainer,
                recyclerViewItems,
                newItem -> {
                    newItem.setListId(currentListId);
                    viewModel.insertItem(newItem);
                }
        );

        quickItemsManager.setup(currentListId);
    }

    private void setupFab() {
        fabAddItem.setOnClickListener(v -> showAddItemDialog());
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                viewModel.forceFullSync();

                // Fallback: Stop refreshing after 5 seconds if viewModel doesn't stop it
                new Handler().postDelayed(() -> {
                    if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }, SYNC_FALLBACK_TIMEOUT_MS);
            });
        }
    }

    // ===== ITEM INTERACTION CALLBACKS =====

    private void onItemClick(GroceryItem item) {
        showEditItemDialog(item);
    }

    private void onItemCompleteToggle(GroceryItem item) {
        item.setCompleted(!item.isCompleted());
        item.setUpdatedAt(System.currentTimeMillis());
        viewModel.updateItem(item);
    }

    private void onItemLongClick(GroceryItem item) {
        contextMenuHandler.showContextMenu(item);
    }

    // ===== DIALOG METHODS =====

    private void showAddItemDialog() {
        AddItemDialogFragment dialog = new AddItemDialogFragment();
        dialog.setOnItemAddedListener(item -> {
            item.setListId(currentListId);
            viewModel.insertItem(item);
        });
        dialog.show(getSupportFragmentManager(), "add_item");
    }

    private void showEditItemDialog(GroceryItem item) {
        EditItemDialogFragment dialog = EditItemDialogFragment.newInstance(item);
        dialog.setOnItemUpdatedListener(updatedItem -> viewModel.updateItem(updatedItem));
        dialog.show(getSupportFragmentManager(), "edit_item");
    }

    private void showClearCompletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.clear_completed))
                .setMessage(getString(R.string.clear_completed_message))
                .setPositiveButton(getString(R.string.clear_completed), (dialog, which) -> viewModel.clearCompletedItems(currentListId))
                .setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {
                getString(R.string.sorted_alphabetically),
                getString(R.string.sort_rema1000)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sort_by));
        builder.setItems(sortOptions, (dialog, which) -> {
            adapter.setSortType(which);

            String feedbackMessage;
            switch (which) {
                case 0:
                    feedbackMessage = getString(R.string.sorted_alphabetically);
                    break;
                case 1:
                    feedbackMessage = getString(R.string.sorted_by_rema1000);
                    break;
                default:
                    feedbackMessage = getString(R.string.sorted);
            }

            Snackbar.make(recyclerViewItems, feedbackMessage, Snackbar.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // ===== MENU =====

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuId = menuItem.getItemId();

        if (menuId == android.R.id.home) {
            finish();
            return true;
        } else if (menuId == R.id.action_clear_completed) {
            showClearCompletedDialog();
            return true;
        } else if (menuId == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (menuId == R.id.action_sync) {
            viewModel.forceFullSync();
            return true;
        } else if (menuId == R.id.action_reset_quick_items) {
            quickItemsManager.showResetDialog();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    // ===== HELPER METHODS =====

    private void updateSyncInfo() {
        if (textSyncInfo != null) {
            textSyncInfo.setText(viewModel.getLastSyncInfo());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSyncInfo();
    }
}