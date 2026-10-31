package com.buggybank.buggy_bank.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.buggybank.buggy_bank.common.money.Money;
import com.buggybank.buggy_bank.transaction.entity.Transaction;
import com.buggybank.buggy_bank.transaction.entity.TransactionType;

public record TransactionResponse(UUID id, UUID accountId, TransactionType type,
                                  BigDecimal amount, LocalDate occurredOn, String note) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountId(), t.getType(),
                Money.toRupees(t.getAmountPaise()), t.getOccurredOn(), t.getNote());
    }
}