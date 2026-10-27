package com.buggybank.buggy_bank.common.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class UserTimeTest {

    // 19:00 UTC on 26 Oct is already 00:30 on 27 Oct in India
    private static final Instant LATE_EVENING_UTC = Instant.parse("2026-10-26T19:00:00Z");

    @Test
    void sameInstantIsDifferentDaysInDifferentZones() {
        assertEquals(LocalDate.of(2026, 10, 26), UserTime.toLocalDate(LATE_EVENING_UTC, ZoneId.of("UTC")));
        assertEquals(LocalDate.of(2026, 10, 27), UserTime.toLocalDate(LATE_EVENING_UTC, ZoneId.of("Asia/Kolkata")));
    }

    @Test
    void weekStartsOnMonday() {
        assertEquals(LocalDate.of(2026, 10, 26), UserTime.startOfWeek(LocalDate.of(2026, 10, 28)));
    }
}