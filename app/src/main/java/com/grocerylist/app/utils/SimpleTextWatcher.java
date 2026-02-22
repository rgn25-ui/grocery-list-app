package com.grocerylist.app.utils;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Convenience base class for TextWatcher that provides no-op default implementations,
 * allowing subclasses to override only the methods they need.
 */
public abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op by design - override if needed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No-op by design - override if needed
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No-op by design - override if needed
    }
}