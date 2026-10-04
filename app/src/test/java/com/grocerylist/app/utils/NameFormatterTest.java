package com.grocerylist.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NameFormatterTest {

    @Test
    public void firstLetterIsUpperCased() {
        assertEquals("Mælk", NameFormatter.capitalizeFirst("mælk"));
        assertEquals("Hakket oksekød", NameFormatter.capitalizeFirst("hakket oksekød"));
    }

    @Test
    public void danishLettersAreHandled() {
        assertEquals("Æg", NameFormatter.capitalizeFirst("æg"));
        assertEquals("Ølandshvede", NameFormatter.capitalizeFirst("ølandshvede"));
        assertEquals("Ål", NameFormatter.capitalizeFirst("ål"));
    }

    @Test
    public void restOfNameIsUntouched() {
        assertEquals("Rugbrød", NameFormatter.capitalizeFirst("Rugbrød"));
        assertEquals("KÆRGÅRDEN", NameFormatter.capitalizeFirst("KÆRGÅRDEN"));
        assertEquals("3 stjernet pålæg", NameFormatter.capitalizeFirst("3 stjernet pålæg"));
    }

    @Test
    public void emptyAndNullAreKept() {
        assertEquals("", NameFormatter.capitalizeFirst(""));
        assertNull(NameFormatter.capitalizeFirst(null));
    }
}