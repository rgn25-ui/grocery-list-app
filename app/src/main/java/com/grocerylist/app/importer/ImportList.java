package com.grocerylist.app.importer;

import com.grocerylist.app.models.ListCategory;

import java.util.Collections;
import java.util.List;

/** One validated list from an import payload. */
public final class ImportList {

    private final String name;          // null = the user chooses the target list
    private final ListCategory store;   // null = the app's default store
    private final List<ImportItem> items;

    ImportList(String name, ListCategory store, List<ImportItem> items) {
        this.name = name;
        this.store = store;
        this.items = Collections.unmodifiableList(items);
    }

    public String getName() { return name; }
    public ListCategory getStore() { return store; }
    public List<ImportItem> getItems() { return items; }
}
