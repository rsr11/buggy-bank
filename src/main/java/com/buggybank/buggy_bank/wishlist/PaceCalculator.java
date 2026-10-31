package com.buggybank.buggy_bank.wishlist;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class PaceCalculator {

    private PaceCalculator() {}

    /** Paise needed per week to reach the target by targetDate, or null if there's no target date. */
    public static Long weeklyNeededPaise(long targetPaise, long savedPaise, LocalDate today, LocalDate targetDate) {
        long remaining = targetPaise - savedPaise;
        if (remaining <= 0) {
            return 0L;                       // already reached
        }
        if (targetDate == null) {
            return null;                     // no deadline, no pace
        }
        long days = ChronoUnit.DAYS.between(today, targetDate);
        if (days <= 0) {
            return remaining;                // due today or overdue: all of it is needed now
        }
        long weeks = Math.max(1, (days + 6) / 7);    // round partial weeks up
        return Math.ceilDiv(remaining, weeks);       // round money up so you never fall short
    }
}