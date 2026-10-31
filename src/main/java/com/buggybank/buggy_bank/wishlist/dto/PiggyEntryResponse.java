package com.buggybank.buggy_bank.wishlist.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.buggybank.buggy_bank.common.money.Money;
import com.buggybank.buggy_bank.wishlist.entity.PiggyEntry;
import com.buggybank.buggy_bank.wishlist.entity.PiggyEntryType;

public record PiggyEntryResponse(UUID id, PiggyEntryType type, BigDecimal amount, LocalDate entryDate, String note) {

    public static PiggyEntryResponse from(PiggyEntry e) {
        return new PiggyEntryResponse(e.getId(), e.getType(), Money.toRupees(e.getAmountPaise()),
                e.getEntryDate(), e.getNote());
    }
}