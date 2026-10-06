package com.grocerylist.app.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtilsTest {

    private static final ZoneId CPH = ZoneId.of("Europe/Copenhagen");
    private static final long NOW = millis(2026, 10, 6, 21, 0);

    private static long millis(int y, int mo, int d, int h, int mi) {
        return ZonedDateTime.of(y, mo, d, h, mi, 0, 0, CPH).toInstant().toEpochMilli();
    }

    @Test
    public void today_showsClockOnly() {
        assertEquals("i dag kl. 17:42", DateUtils.formatSyncTime(millis(2026, 10, 6, 17, 42), NOW, CPH));
    }

    @Test
    public void yesterday_saysIGaar() {
        assertEquals("i går kl. 09:05", DateUtils.formatSyncTime(millis(2026, 10, 5, 9, 5), NOW, CPH));
    }

    @Test
    public void earlierThisYear_hasNoYear() {
        assertEquals("1. okt. kl. 08:00", DateUtils.formatSyncTime(millis(2026, 10, 1, 8, 0), NOW, CPH));
    }

    @Test
    public void previousYear_includesYear() {
        assertEquals("30. dec. 2025 kl. 12:00", DateUtils.formatSyncTime(millis(2025, 12, 30, 12, 0), NOW, CPH));
    }

    @Test
    public void justAfterMidnight_isStillToday() {
        long now = millis(2026, 10, 6, 0, 30);
        assertEquals("i går kl. 23:59", DateUtils.formatSyncTime(millis(2026, 10, 5, 23, 59), now, CPH));
        assertEquals("i dag kl. 00:10", DateUtils.formatSyncTime(millis(2026, 10, 6, 0, 10), now, CPH));
    }
}