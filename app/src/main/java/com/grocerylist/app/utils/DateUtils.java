package com.grocerylist.app.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static final Locale DANISH = Locale.forLanguageTag("da-DK");

    // ===== DATE FORMATTERS =====
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd. MMM yyyy", DANISH)
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

    // ===== SYNC TIME =====

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", DANISH);

    private static final DateTimeFormatter DAY_MONTH_FORMAT =
            DateTimeFormatter.ofPattern("d. MMM", DANISH);

    private static final DateTimeFormatter DAY_MONTH_YEAR_FORMAT =
            DateTimeFormatter.ofPattern("d. MMM yyyy", DANISH);

    /**
     * Short time for the sync status line: "i dag kl. 17:42", "i går kl. 09:10",
     * "6. okt. kl. 17:42", and the year only when it is not the current year.
     */
    public static String formatSyncTime(long timestamp) {
        return formatSyncTime(timestamp, System.currentTimeMillis(), ZoneId.systemDefault());
    }

    static String formatSyncTime(long timestamp, long now, ZoneId zone) {
        ZonedDateTime time = Instant.ofEpochMilli(timestamp).atZone(zone);
        LocalDate date = time.toLocalDate();
        LocalDate today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        String clock = TIME_FORMAT.format(time);

        if (date.equals(today)) {
            return "i dag kl. " + clock;
        }
        if (date.equals(today.minusDays(1))) {
            return "i går kl. " + clock;
        }
        if (date.getYear() == today.getYear()) {
            return DAY_MONTH_FORMAT.format(time) + " kl. " + clock;
        }
        return DAY_MONTH_YEAR_FORMAT.format(time) + " kl. " + clock;
    }

}