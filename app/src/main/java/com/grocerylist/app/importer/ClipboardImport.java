package com.grocerylist.app.importer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.grocerylist.app.R;

/**
 * Starts an import from the clipboard. The clipboard is read here, in the calling activity,
 * because Android 10+ only allows the app with window focus to read it - ImportActivity
 * does not have focus yet in onCreate. The text is handed over as an ordinary text share,
 * so ImportActivity treats it exactly like text shared from another app.
 */
public final class ClipboardImport {

    private ClipboardImport() {
    }

    public static void start(Activity activity) {
        String text = readClipboard(activity);
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(activity, R.string.import_clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        // Also keeps the intent far below the Binder transaction limit
        if (text.length() > ImportTextExtractor.MAX_INPUT_CHARS) {
            Toast.makeText(activity, R.string.import_clipboard_too_long, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(activity, ImportActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(intent);
    }

    private static String readClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            return null;
        }
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return null;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(context);
        return text != null ? text.toString() : null;
    }
}