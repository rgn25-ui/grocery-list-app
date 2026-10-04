package com.grocerylist.app.importer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.grocerylist.app.ListDetailActivity;
import com.grocerylist.app.R;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.ListCategory;
import com.grocerylist.app.utils.NameFormatter;
import com.grocerylist.app.viewmodel.GroceryViewModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Share target for text/plain. Parses and validates the shared JSON, shows a confirmation
 * screen, and adds the selected items through the normal local-first repository path.
 * Nothing is written before the user confirms.
 */
public class ImportActivity extends AppCompatActivity {

    private static final int COLOR_ERROR = 0xFFC62828;
    private static final int COLOR_WARNING = 0xFF8A5A00;
    private static final int COLOR_OFFER = 0xFFD32F2F;
    private static final int COLOR_MUTED = 0xFF616161;
    private static final String UNCERTAIN_NOTE = "usikker";

    private GroceryViewModel viewModel;
    private ImportResult result;
    private final List<GroceryList> existingLists = new ArrayList<>();
    private final List<TargetSection> sections = new ArrayList<>();
    private boolean confirmationShown = false;
    private GroceryList listToOpen;

    private TextView textStatus;
    private LinearLayout container;
    private Button buttonImport;
    private Button buttonCancel;

    /** One list from the payload, its resolved target and the item checkboxes. */
    private static final class TargetSection {
        final ImportList source;
        final GroceryList matchedList;   // existing list with the same name, or null
        Spinner listSpinner;             // only for lists without a name
        final List<CheckBox> checkBoxes = new ArrayList<>();

        TargetSection(ImportList source, GroceryList matchedList) {
            this.source = source;
            this.matchedList = matchedList;
        }

        List<ImportItem> selectedItems() {
            List<ImportItem> selected = new ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    selected.add(source.getItems().get(i));
                }
            }
            return selected;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        textStatus = findViewById(R.id.text_import_status);
        container = findViewById(R.id.layout_import_container);
        buttonImport = findViewById(R.id.button_import);
        buttonCancel = findViewById(R.id.button_import_cancel);
        buttonCancel.setOnClickListener(v -> finish());

        result = ImportParser.parse(readSharedText());
        if (!result.isValid()) {
            showRejected();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GroceryViewModel.class);
        viewModel.getError().observe(this, this::onImportError);
        viewModel.getImportedCount().observe(this, this::onImportDone);
        viewModel.getAllLists().observe(this, lists -> {
            // Resolve targets once; later database changes must not rebuild the screen
            if (confirmationShown) {
                return;
            }
            confirmationShown = true;
            existingLists.addAll(lists != null ? lists : Collections.emptyList());
            showConfirmation();
        });
    }

    private String readSharedText() {
        Intent intent = getIntent();
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            return null;
        }
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        return text != null ? text.toString() : null;
    }

    // ===== REJECTED =====

    private void showRejected() {
        textStatus.setText(R.string.import_rejected);
        for (String error : result.getErrors()) {
            addMessage("• " + error, COLOR_ERROR);
        }
        buttonImport.setVisibility(View.GONE);
        buttonCancel.setText(R.string.import_close);
    }

    // ===== CONFIRMATION =====

    private void showConfirmation() {
        textStatus.setText(getString(R.string.import_ready, result.getItemCount()));
        for (String warning : result.getWarnings()) {
            addMessage("• " + warning, COLOR_WARNING);
        }
        for (ImportList list : result.getLists()) {
            addSection(list);
        }
        buttonImport.setOnClickListener(v -> performImport());
    }

    private void addSection(ImportList list) {
        GroceryList matched = list.getName() != null ? findListByName(list.getName()) : null;
        TargetSection section = new TargetSection(list, matched);

        TextView header = new TextView(this);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextSize(16);
        header.setPadding(0, dp(20), 0, dp(4));
        container.addView(header);

        if (list.getName() == null) {
            header.setText(R.string.import_target_choose);
            section.listSpinner = createListSpinner(storeFor(list));
            container.addView(section.listSpinner);
        } else if (matched != null) {
            header.setText(getString(R.string.import_target_existing,
                    storeName(matched.getCategory()), matched.getName()));
        } else {
            header.setText(getString(R.string.import_target_new,
                    storeFor(list).getDisplayName(), list.getName()));
        }

        for (ImportItem item : list.getItems()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(true);
            checkBox.setText(describe(item));
            checkBox.setPadding(dp(4), dp(6), 0, dp(6));
            container.addView(checkBox);
            section.checkBoxes.add(checkBox);
        }
        sections.add(section);
    }

    /** Existing lists (most recently updated first), then "Ny liste". */
    private Spinner createListSpinner(ListCategory storeForNewList) {
        List<String> labels = new ArrayList<>();
        for (GroceryList list : existingLists) {
            labels.add(storeName(list.getCategory()) + ": " + list.getName());
        }
        labels.add(getString(R.string.import_new_list_option, storeForNewList.getDisplayName()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        return spinner;
    }

    /** Name and amount, offer and price, and the note on its own line - "usikker" highlighted. */
    private CharSequence describe(ImportItem item) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        append(text, NameFormatter.capitalizeFirst(item.getName()), new StyleSpan(Typeface.BOLD));
        text.append("   ").append(ImportItemMapper.formatAmount(item));

        if (item.isOnOffer()) {
            text.append("   ");
            append(text, getString(R.string.tilbud_label), new StyleSpan(Typeface.BOLD),
                    new ForegroundColorSpan(COLOR_OFFER));
        }
        if (item.getPrice() != null) {
            boolean perPiece = item.getUnit() == null && item.getQuantity().compareTo(BigDecimal.ONE) > 0;
            String price = ImportItemMapper.formatPrice(item.getPrice()) + " kr";
            text.append("   ").append(perPiece ? "à " + price : price);
        }
        if (item.getNote() != null) {
            text.append("\n");
            if (UNCERTAIN_NOTE.equalsIgnoreCase(item.getNote())) {
                append(text, "⚠ " + getString(R.string.import_uncertain), new StyleSpan(Typeface.BOLD),
                        new ForegroundColorSpan(COLOR_WARNING), new RelativeSizeSpan(0.9f));
            } else {
                append(text, item.getNote(), new StyleSpan(Typeface.ITALIC),
                        new ForegroundColorSpan(COLOR_MUTED), new RelativeSizeSpan(0.9f));
            }
        }
        return text;
    }

    // ===== IMPORT =====

    private void performImport() {
        List<GroceryList> newLists = new ArrayList<>();
        Map<String, GroceryList> newListsByName = new HashMap<>();
        List<GroceryItem> items = new ArrayList<>();
        GroceryList firstTarget = null;

        for (TargetSection section : sections) {
            List<ImportItem> selected = section.selectedItems();
            if (selected.isEmpty()) {
                continue;
            }
            GroceryList target = resolveTarget(section, newLists, newListsByName);
            for (ImportItem item : selected) {
                items.add(ImportItemMapper.toGroceryItem(item, target.getId()));
            }
            if (firstTarget == null) {
                firstTarget = target;
            }
        }

        if (items.isEmpty()) {
            Toast.makeText(this, R.string.import_nothing_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        buttonImport.setEnabled(false);
        listToOpen = firstTarget;
        viewModel.importItems(newLists, items);
    }

    private GroceryList resolveTarget(TargetSection section, List<GroceryList> newLists,
                                      Map<String, GroceryList> newListsByName) {
        ImportList source = section.source;
        if (source.getName() == null) {
            int position = section.listSpinner.getSelectedItemPosition();
            if (position >= 0 && position < existingLists.size()) {
                return existingLists.get(position);
            }
            return createList(getString(R.string.default_list_name), storeFor(source), newLists);
        }
        if (section.matchedList != null) {
            return section.matchedList;
        }
        // Two sections with the same new name share one list
        String key = source.getName().toLowerCase(Locale.ROOT);
        GroceryList existingNew = newListsByName.get(key);
        if (existingNew != null) {
            return existingNew;
        }
        GroceryList created = createList(source.getName(), storeFor(source), newLists);
        newListsByName.put(key, created);
        return created;
    }

    private GroceryList createList(String name, ListCategory store, List<GroceryList> newLists) {
        GroceryList list = new GroceryList(name);
        list.setCategory(store.name());
        newLists.add(list);
        return list;
    }

    private void onImportDone(Integer count) {
        if (count == null) {
            return;
        }
        Toast.makeText(this, getString(R.string.import_done, count), Toast.LENGTH_SHORT).show();
        if (listToOpen != null) {
            Intent intent = new Intent(this, ListDetailActivity.class);
            intent.putExtra("list_id", listToOpen.getId());
            intent.putExtra("list_name", listToOpen.getName());
            intent.putExtra("list_category", listToOpen.getCategory());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void onImportError(String message) {
        if (message == null) {
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        buttonImport.setEnabled(true);
    }

    // ===== HELPERS =====

    private GroceryList findListByName(String name) {
        for (GroceryList list : existingLists) {
            if (list.getName() != null && list.getName().trim().equalsIgnoreCase(name.trim())) {
                return list;
            }
        }
        return null;
    }

    private static ListCategory storeFor(ImportList list) {
        return list.getStore() != null ? list.getStore() : ListCategory.REMA;
    }

    private static String storeName(String category) {
        return ListCategory.getCategoryByName(category).getDisplayName();
    }

    private void addMessage(String message, int color) {
        TextView view = new TextView(this);
        view.setText(message);
        view.setTextColor(color);
        view.setPadding(0, dp(4), 0, dp(4));
        container.addView(view);
    }

    private static void append(SpannableStringBuilder builder, String text, Object... spans) {
        int start = builder.length();
        builder.append(text);
        for (Object span : spans) {
            builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}