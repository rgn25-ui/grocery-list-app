package com.grocerylist.app.importer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grocerylist.app.models.ListCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validates a parsed import payload against grocery-import-schema-v1.json,
 * plus the rules the schema cannot express (total item cap, merging duplicates).
 * Collects every error instead of stopping at the first one.
 */
final class ImportValidator {

    static final int MAX_LISTS = 5;
    static final int MAX_ITEMS_TOTAL = 50;
    static final int LIST_NAME_MAX = 40;
    static final int ITEM_NAME_MAX = 60;
    static final int NOTE_MAX = 100;
    static final int PIECE_MAX = 99;
    static final BigDecimal AMOUNT_MAX = new BigDecimal(5000);
    static final BigDecimal PRICE_MAX = new BigDecimal(10000);

    private static final Set<String> ROOT_KEYS = setOf("version", "lists");
    private static final Set<String> LIST_KEYS = setOf("name", "store", "items");
    private static final Set<String> ITEM_KEYS = setOf("name", "quantity", "unit", "note", "price", "onOffer");
    private static final Set<String> UNITS = setOf("g", "kg", "ml", "l");
    private static final Locale DANISH = Locale.forLanguageTag("da-DK");

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private ImportValidator() {
    }

    static ImportResult validate(JsonElement root) {
        return new ImportValidator().run(root);
    }

    private ImportResult run(JsonElement root) {
        List<ImportList> lists = new ArrayList<>();
        if (root == null || !root.isJsonObject()) {
            errors.add("Roden skal være et JSON-objekt.");
            return result(lists);
        }
        JsonObject obj = root.getAsJsonObject();
        checkKeys(obj, ROOT_KEYS, "");
        checkVersion(obj);

        if (!obj.has("lists")) {
            errors.add("Feltet lists mangler.");
            return result(lists);
        }
        JsonElement listsElement = obj.get("lists");
        if (!listsElement.isJsonArray()) {
            errors.add("lists skal være en liste.");
            return result(lists);
        }
        JsonArray listArray = listsElement.getAsJsonArray();
        if (listArray.isEmpty()) {
            errors.add("lists skal indeholde mindst én liste.");
        }
        if (listArray.size() > MAX_LISTS) {
            errors.add("Der er " + listArray.size() + " lister. Højst " + MAX_LISTS + " er tilladt.");
        }

        int total = 0;
        for (int i = 0; i < listArray.size(); i++) {
            String path = "lists[" + i + "]";
            JsonElement element = listArray.get(i);
            if (!element.isJsonObject()) {
                errors.add(path + " skal være et objekt.");
                continue;
            }
            JsonObject listObj = element.getAsJsonObject();
            checkKeys(listObj, LIST_KEYS, path);
            String name = optionalText(listObj, "name", path, LIST_NAME_MAX);
            ListCategory store = optionalStore(listObj, path);

            if (!listObj.has("items")) {
                errors.add(path + ".items mangler.");
                continue;
            }
            JsonElement itemsElement = listObj.get("items");
            if (!itemsElement.isJsonArray()) {
                errors.add(path + ".items skal være en liste.");
                continue;
            }
            JsonArray itemArray = itemsElement.getAsJsonArray();
            if (itemArray.isEmpty()) {
                errors.add(path + ".items skal indeholde mindst én vare.");
            }
            total += itemArray.size();

            List<ImportItem> items = new ArrayList<>();
            for (int j = 0; j < itemArray.size(); j++) {
                ImportItem item = validateItem(itemArray.get(j), path + ".items[" + j + "]");
                if (item != null) {
                    addOrMerge(items, item, name);
                }
            }
            checkMergedQuantities(items);
            lists.add(new ImportList(name, store, items));
        }

        if (total > MAX_ITEMS_TOTAL) {
            errors.add("Der er " + total + " varer i alt. Højst " + MAX_ITEMS_TOTAL + " er tilladt.");
        }
        return result(lists);
    }

    private ImportResult result(List<ImportList> lists) {
        return new ImportResult(errors, warnings, errors.isEmpty() ? lists : new ArrayList<>());
    }

    // ===== ITEMS =====

    private ImportItem validateItem(JsonElement element, String path) {
        if (!element.isJsonObject()) {
            errors.add(path + " skal være et objekt.");
            return null;
        }
        JsonObject obj = element.getAsJsonObject();
        int errorsBefore = errors.size();
        checkKeys(obj, ITEM_KEYS, path);

        String name = requiredName(obj, path);
        String note = optionalText(obj, "note", path, NOTE_MAX);

        boolean hasUnit = obj.has("unit");
        String unit = null;
        if (hasUnit) {
            JsonElement u = obj.get("unit");
            if (isString(u) && UNITS.contains(u.getAsString())) {
                unit = u.getAsString();
            } else {
                errors.add(path + ".unit er " + u + ". Tilladt: g, kg, ml, l.");
            }
        }

        BigDecimal quantity = BigDecimal.ONE;
        if (unit != null && !obj.has("quantity")) {
            errors.add(path + ".quantity mangler. Den er påkrævet, når der er en enhed.");
        }
        if (obj.has("quantity")) {
            JsonElement q = obj.get("quantity");
            if (isNotNumber(q)) {
                errors.add(path + ".quantity skal være et tal, men er " + q + ".");
            } else {
                BigDecimal value = q.getAsBigDecimal();
                String shown = value.toPlainString();
                if (!hasUnit) {
                    if (!isWholeNumber(value)) {
                        errors.add(path + ".quantity er " + shown + ". Uden enhed skal antal være et helt tal.");
                    } else if (value.compareTo(BigDecimal.ONE) < 0 || value.compareTo(BigDecimal.valueOf(PIECE_MAX)) > 0) {
                        errors.add(path + ".quantity er " + shown + ". Antal stk skal være mellem 1 og " + PIECE_MAX + ".");
                    } else {
                        quantity = value;
                    }
                } else if (unit != null) {
                    if (value.signum() <= 0 || value.compareTo(AMOUNT_MAX) > 0) {
                        errors.add(path + ".quantity er " + shown + ". Med enhed skal mængden være over 0 og højst "
                                + AMOUNT_MAX + ".");
                    } else {
                        quantity = value;
                    }
                }
            }
        }

        BigDecimal price = null;
        if (obj.has("price")) {
            JsonElement p = obj.get("price");
            if (isNotNumber(p)) {
                errors.add(path + ".price skal være et tal, men er " + p + ".");
            } else {
                BigDecimal value = p.getAsBigDecimal();
                String shown = value.toPlainString();
                if (value.signum() <= 0 || value.compareTo(PRICE_MAX) > 0) {
                    errors.add(path + ".price er " + shown + ". Prisen skal være over 0 og højst " + PRICE_MAX + " kr.");
                } else if (value.stripTrailingZeros().scale() > 2) {
                    errors.add(path + ".price er " + shown + ". Højst 2 decimaler er tilladt.");
                } else {
                    price = value;
                }
            }
        }

        boolean onOffer = false;
        if (obj.has("onOffer")) {
            JsonElement o = obj.get("onOffer");
            if (o.isJsonPrimitive() && o.getAsJsonPrimitive().isBoolean()) {
                onOffer = o.getAsBoolean();
            } else {
                errors.add(path + ".onOffer skal være true eller false, men er " + o + ".");
            }
        }

        if (errors.size() > errorsBefore) {
            return null;
        }
        return new ImportItem(name, quantity, unit, note, price, onOffer);
    }

    /** Merges equal lines (same name and unit, same details); keeps differing ones and warns. */
    private void addOrMerge(List<ImportItem> items, ImportItem item, String listName) {
        String key = itemKey(item);
        String where = listName != null ? " på " + listName : "";
        for (int i = 0; i < items.size(); i++) {
            ImportItem existing = items.get(i);
            if (!itemKey(existing).equals(key)) {
                continue;
            }
            if (existing.hasSameDetails(item)) {
                items.set(i, existing.withQuantity(existing.getQuantity().add(item.getQuantity())));
                warnings.add("“" + item.getName() + "” står flere gange" + where + " og slås sammen.");
                return;
            }
            warnings.add("“" + item.getName() + "” står flere gange" + where
                    + " med forskellige oplysninger. Begge linjer vises.");
            break;
        }
        items.add(item);
    }

    private void checkMergedQuantities(List<ImportItem> items) {
        for (ImportItem item : items) {
            BigDecimal max = item.getUnit() == null ? BigDecimal.valueOf(PIECE_MAX) : AMOUNT_MAX;
            if (item.getQuantity().compareTo(max) > 0) {
                errors.add("“" + item.getName() + "” får mængden " + item.getQuantity().toPlainString()
                        + " efter sammenlægning. Højst " + max + " er tilladt.");
            }
        }
    }

    private static String itemKey(ImportItem item) {
        return item.getName().toLowerCase(DANISH) + "|" + (item.getUnit() == null ? "" : item.getUnit());
    }

    // ===== FIELD HELPERS =====

    private void checkVersion(JsonObject obj) {
        if (!obj.has("version")) {
            errors.add("Feltet version mangler.");
            return;
        }
        JsonElement v = obj.get("version");
        if (isNotNumber(v) || v.getAsBigDecimal().compareTo(BigDecimal.ONE) != 0) {
            errors.add("version skal være 1, men er " + v + ".");
        }
    }

    private ListCategory optionalStore(JsonObject obj, String path) {
        if (!obj.has("store")) {
            return null;
        }
        JsonElement s = obj.get("store");
        if (isString(s)) {
            for (ListCategory category : ListCategory.values()) {
                if (category.name().equals(s.getAsString())) {
                    return category;
                }
            }
        }
        errors.add(path + ".store er " + s + ". Tilladt: REMA, COOP, ANDRE.");
        return null;
    }

    private String requiredName(JsonObject obj, String path) {
        if (!obj.has("name")) {
            errors.add(path + ".name mangler.");
            return null;
        }
        return text(obj.get("name"), path + ".name", ITEM_NAME_MAX);
    }

    private String optionalText(JsonObject obj, String key, String path, int max) {
        return obj.has(key) ? text(obj.get(key), path + "." + key, max) : null;
    }

    private String text(JsonElement element, String path, int max) {
        if (!isString(element)) {
            errors.add(path + " skal være tekst.");
            return null;
        }
        String value = normalize(element.getAsString());
        if (value.isEmpty()) {
            errors.add(path + " er tom.");
            return null;
        }
        if (value.length() > max) {
            errors.add(path + " er " + value.length() + " tegn. Højst " + max + " er tilladt.");
            return null;
        }
        return value;
    }

    private void checkKeys(JsonObject obj, Set<String> allowed, String path) {
        for (String key : obj.keySet()) {
            if (!allowed.contains(key)) {
                errors.add("Ukendt felt " + (path.isEmpty() ? key : path + "." + key) + ".");
            }
        }
    }

    static String normalize(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

    private static boolean isString(JsonElement e) {
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isString();
    }

    private static boolean isNotNumber(JsonElement e) {
        return e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber();
    }

    private static boolean isWholeNumber(BigDecimal value) {
        return value.signum() == 0 || value.stripTrailingZeros().scale() <= 0;
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}