package com.buggybank.buggy_bank.common.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void rupeesToPaise() {
        assertEquals(123456L, Money.toPaise(new BigDecimal("1234.56")));
    }

    @Test
    void paiseToRupees() {
        assertEquals(new BigDecimal("1234.56"), Money.toRupees(123456L));
    }

    @Test
    void halfPaiseRoundsUp() {
        assertEquals(1L, Money.toPaise(new BigDecimal("0.005")));
    }
}