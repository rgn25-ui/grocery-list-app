package com.grocerylist.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.grocerylist.app.adapters.GroceryListAdapter;
import com.grocerylist.app.models.GroceryItemSuggestions;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.ui.dialogs.ListDialogManager;
import com.grocerylist.app.viewmodel.GroceryViewModel;

/**
 * Main activity displaying all grocery lists
 * Refactored to use ListDialogManager for cleaner separation of concerns
 */
public class MainActivity extends AppCompatActivity {

    private GroceryListAdapter adapter;
    private GroceryViewModel viewModel;

    // Views
    private RecyclerView recyclerViewLists;
    private View emptyView;
    private FloatingActionButton fabAddList;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textSyncInfo;
    private LoadingMessageManager loadingMessageManager;

    // Dialog Manager
    private ListDialogManager dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GroceryItemSuggestions.initialize(this);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        setupViews();
        setupToolbar();
        setupLoadingMessages();
        setupDialogManager();
        setupViewModel();
        setupRecyclerView();
        setupFab();
        setupSwipeRefresh();

        // Keep screen on when app is active
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Auto-sync on app start
        viewModel.smartSync();
    }

    private void setupViews() {
        recyclerViewLists = findViewById(R.id.recycler_view_lists);
        emptyView = findViewById(R.id.empty_view);
        fabAddList = findViewById(R.id.fab_add_list);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        textSyncInfo = findViewById(R.id.text_sync_info);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.all_lists_with_icon, getString(R.string.all_lists)));
        }
    }

    private void setupLoadingMessages() {
        View loadingOverlay = findViewById(R.id.loading_overlay);
        TextView loadingMessage = findViewById(R.id.loading_message);
        loadingMessageManager = new LoadingMessageManager(loadingOverlay, loadingMessage);
    }

    private void setupDialogManager() {
        dialogManager = new ListDialogManager(this, new ListDialogManager.OnListDialogListener() {
            @Override
            public void onListCreated(String name, String category) {
                GroceryList newList = new GroceryList(name);
                newList.setCategory(category);
                viewModel.insertList(newList);
            }

            @Override
            public void onListDuplicated(String originalListId, String newName, String category) {
                viewModel.duplicateList(originalListId, newName, category);
            }

            @Override
            public void onListRenamed(GroceryList list, String newName, String newCategory) {
                list.setName(newName);
                list.setCategory(newCategory);
                list.setUpdatedAt(System.currentTimeMillis());
                viewModel.updateList(list);
            }

            @Override
            public void onClearAllDataConfirmed() {
                viewModel.clearAllData();
                Snackbar.make(recyclerViewLists, getString(R.string.clearing_all_data),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new GroceryListAdapter(
                this::onListClick,
                this::onListLongClick,
                viewModel.getRepository(),
                this
        );
        recyclerViewLists.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLists.setAdapter(adapter);

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                GroceryList list = adapter.getListAt(position);
                viewModel.deleteList(list.getId());

                // Show undo snack bar
                Snackbar.make(recyclerViewLists, getString(R.string.list_deleted), Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            list.setDeleted(false);
                            viewModel.insertList(list);
                        })
                        .show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerViewLists);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GroceryViewModel.class);

        // Observe lists
        viewModel.getAllLists().observe(this, lists -> {
            adapter.submitList(lists);
            emptyView.setVisibility(lists == null || lists.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(recyclerViewLists, error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe sync status
        viewModel.getSyncStatus().observe(this, status -> {
            if (status != null) {
                Snackbar.make(recyclerViewLists, status, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Observe refresh state
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

        // Initial sync info display
        updateSyncInfo();
    }

    private void setupFab() {
        fabAddList.setOnClickListener(v -> dialogManager.showAddListDialog());
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> viewModel.forceFullSync());
        }
    }

    // ===== LIST INTERACTION CALLBACKS =====

    private void onListClick(GroceryList list) {
        Intent intent = new Intent(this, ListDetailActivity.class);
        intent.putExtra("list_id", list.getId());
        intent.putExtra("list_name", list.getName());
        intent.putExtra("list_category", list.getCategory());
        startActivity(intent);
    }

    private void onListLongClick(GroceryList list, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.list_context_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            if (menuId == R.id.action_duplicate) {
                dialogManager.showDuplicateListDialog(list);
                return true;
            } else if (menuId == R.id.action_rename) {
                dialogManager.showRenameListDialog(list);
                return true;
            } else if (menuId == R.id.action_delete) {
                viewModel.deleteList(list.getId());
                return true;
            }
            return false;
        });

        popup.show();
    }

    // ===== MENU HANDLING =====

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_sync) {
            viewModel.forceFullSync();
            return true;
        } else if (menuItem.getItemId() == R.id.action_clear_all) {
            dialogManager.showClearAllDataDialog();
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