package com.buggybank.buggy_bank.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** The database stores money as BIGINT paise. Convert only at the API edge. */
public final class Money {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {}

    public static long toPaise(BigDecimal rupees) {
        return rupees.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static BigDecimal toRupees(long paise) {
        return BigDecimal.valueOf(paise, 2);
    }
}