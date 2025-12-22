package com.grocerylist.app.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    // ===== DATE FORMATTERS =====
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("dd. MMM yyyy", new Locale("da", "DK"));

    private static final SimpleDateFormat DISPLAY_DATE_TIME_FORMAT =
            new SimpleDateFormat("dd. MMM yyyy 'kl.' HH:mm", new Locale("da", "DK"));

    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault());

    static {
        // Set timezone for API format to UTC
        API_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // ===== PRIVATE CONSTRUCTOR =====
    private DateUtils() {
        throw new AssertionError("DateUtils class cannot be instantiated");
    }

    // ===== CURRENT TIME METHODS =====

    /**
     * Get current time in milliseconds
     * @return Current timestamp
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    // ===== FORMATTING METHODS =====

    /**
     * Format timestamp to display date (e.g., "Jan 15, 2024")
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDisplayDate(long timestamp) {
        return DISPLAY_DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to display date and time (e.g., "Jan 15, 2024 at 02:30 PM")
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string
     */
    public static String formatDisplayDateTime(long timestamp) {
        return DISPLAY_DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    // ===== RELATIVE TIME METHODS =====

    /**
     * Get relative time string (e.g., "2 minutes ago", "1 hour ago", "Yesterday")
     * @param timestamp Timestamp in milliseconds
     * @return Relative time string
     */
    public static String getRelativeTimeString(long timestamp) {
        long now = getCurrentTimeMillis();
        long diff = now - timestamp;

        // Future time (shouldn't happen, but handle gracefully)
        if (diff < 0) {
            return "Lige nu";
        }

        // Less than a minute
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Lige nu";
        }

        // Less than an hour
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes == 1 ? "1 minut siden" : minutes + " minutter siden";
        }

        // Less than a day
        if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours == 1 ? "1 time siden" : hours + " timer siden";
        }

        // Less than a week
        if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days == 1) {
                return "I går";
            } else {
                return days + " dage siden";
            }
        }

        // More than a week, show actual date
        return formatDisplayDate(timestamp);
    }










}