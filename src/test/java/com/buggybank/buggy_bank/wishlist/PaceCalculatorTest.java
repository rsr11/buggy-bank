package com.buggybank.buggy_bank.wishlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PaceCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 31);

    @Test
    void sixWeeksLeftRoundsUp() {
        // 42 days = 6 weeks; 10000 / 6 = 1666.67 -> 1667
        assertEquals(1667L, PaceCalculator.weeklyNeededPaise(10000, 0, TODAY, LocalDate.of(2026, 12, 12)));
    }

    @Test
    void alreadyReachedNeedsNothing() {
        assertEquals(0L, PaceCalculator.weeklyNeededPaise(10000, 10000, TODAY, LocalDate.of(2026, 12, 12)));
    }

    @Test
    void noTargetDateMeansNoPace() {
        assertNull(PaceCalculator.weeklyNeededPaise(10000, 0, TODAY, null));
    }

    @Test
    void overdueNeedsEverythingNow() {
        assertEquals(7000L, PaceCalculator.weeklyNeededPaise(10000, 3000, TODAY, LocalDate.of(2026, 10, 1)));
    }

    @Test
    void lessThanAWeekCountsAsOneWeek() {
        assertEquals(7000L, PaceCalculator.weeklyNeededPaise(10000, 3000, TODAY, LocalDate.of(2026, 11, 1)));
    }
}