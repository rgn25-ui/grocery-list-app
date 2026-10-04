package com.grocerylist.app.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pulls the JSON object out of text shared from an AI app.
 * Handles code fences (also nested or broken ones) and prose before or after the JSON.
 */
final class ImportTextExtractor {

    static final int MAX_INPUT_CHARS = 20000;
    private static final Pattern FENCE_LINE = Pattern.compile("^\\s*```[A-Za-z]*\\s*$");

    private ImportTextExtractor() {
    }

    static final class Extraction {
        final String json;
        final String error;

        private Extraction(String json, String error) {
            this.json = json;
            this.error = error;
        }
    }

    static Extraction extract(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            return new Extraction(null, "Der blev ikke delt nogen tekst.");
        }
        if (text.length() > MAX_INPUT_CHARS) {
            return new Extraction(null, "Teksten er for lang (" + text.length()
                    + " tegn). Grænsen er " + MAX_INPUT_CHARS + " tegn.");
        }

        // Drop every line that is only a code fence (```, ```json, ...)
        List<String> kept = new ArrayList<>();
        for (String line : text.split("\\r?\\n", -1)) {
            if (!FENCE_LINE.matcher(line).matches()) {
                kept.add(line);
            }
        }
        String body = String.join("\n", kept);

        int start = body.indexOf('{');
        if (start < 0) {
            return new Extraction(null, "Der blev ikke fundet noget JSON-objekt i teksten.");
        }
        int end = findMatchingBrace(body, start);
        if (end < 0) {
            return new Extraction(null, "JSON-objektet er ikke afsluttet. Der mangler en afsluttende }.");
        }
        return new Extraction(body.substring(start, end + 1), null);
    }

    /** Index of the brace closing the object that starts at {@code start}, ignoring braces inside strings. */
    private static int findMatchingBrace(String s, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
