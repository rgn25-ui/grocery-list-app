package com.grocerylist.app.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static final Locale DANISH = new Locale("da", "DK");

    // ===== DATE FORMATTERS =====
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd. MMM yyyy", DANISH)
                    .withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd. MMM yyyy 'kl.' HH:mm", DANISH)
                    .withZone(ZoneId.systemDefault());

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
     * Format timestamp to display date (e.g., "15. jan 2024")
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDisplayDate(long timestamp) {
        return DISPLAY_DATE_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Format timestamp to display date and time (e.g., "15. jan 2024 kl. 14:30")
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string
     */
    public static String formatDisplayDateTime(long timestamp) {
        return DISPLAY_DATE_TIME_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }

    // ===== RELATIVE TIME METHODS =====

    /**
     * Get relative time string in Danish (e.g., "2 minutter siden", "I går")
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
            return days == 1 ? "I går" : days + " dage siden";
        }

        // More than a week, show actual date
        return formatDisplayDate(timestamp);
    }
}