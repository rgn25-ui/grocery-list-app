package com.grocerylist.app.importer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Entry point for the import: shared text in, validated lists or errors out.
 * The payload is data only - nothing in it is ever executed.
 */
public final class ImportParser {

    private ImportParser() {
    }

    public static ImportResult parse(String sharedText) {
        ImportTextExtractor.Extraction extraction = ImportTextExtractor.extract(sharedText);
        if (extraction.error != null) {
            return ImportResult.failure(extraction.error);
        }

        JsonElement root;
        try {
            root = parseStrict(extraction.json);
        } catch (Exception e) {
            return ImportResult.failure("Teksten er ikke gyldig JSON: " + e.getMessage());
        }
        return ImportValidator.validate(root);
    }

    /**
     * Strict JSON parsing. Gson's JsonParser is lenient (it accepts unquoted strings,
     * single quotes, NaN), so a strict JsonReader is used directly instead.
     */
    @SuppressWarnings("deprecation") // setLenient is deprecated in newer Gson but still supported
    static JsonElement parseStrict(String json) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(false);
        JsonElement element = new Gson().getAdapter(JsonElement.class).read(reader);
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            throw new MalformedJsonException("Uventet tekst efter JSON-objektet");
        }
        return element;
    }
}
