package com.grocerylist.app.importer;

import java.util.Collections;
import java.util.List;

/** Outcome of parsing shared text: either validated lists, or the errors that rejected it. */
public final class ImportResult {

    private final List<String> errors;
    private final List<String> warnings;
    private final List<ImportList> lists;

    ImportResult(List<String> errors, List<String> warnings, List<ImportList> lists) {
        this.errors = Collections.unmodifiableList(errors);
        this.warnings = Collections.unmodifiableList(warnings);
        this.lists = Collections.unmodifiableList(lists);
    }

    static ImportResult failure(String error) {
        return new ImportResult(
                Collections.singletonList(error),
                Collections.emptyList(),
                Collections.emptyList());
    }

    public boolean isValid() {
        return errors.isEmpty() && !lists.isEmpty();
    }

    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public List<ImportList> getLists() { return lists; }

    public int getItemCount() {
        int count = 0;
        for (ImportList list : lists) {
            count += list.getItems().size();
        }
        return count;
    }
}
