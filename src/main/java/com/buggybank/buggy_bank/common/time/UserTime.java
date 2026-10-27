package com.buggybank.buggy_bank.common.time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/** The server runs in UTC. "Which day is it?" is always answered in the user's zone. */
public final class UserTime {

    private UserTime() {}

    public static LocalDate today(ZoneId zone) {
        return LocalDate.now(zone);
    }

    public static LocalDate toLocalDate(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate();
    }

    public static LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}