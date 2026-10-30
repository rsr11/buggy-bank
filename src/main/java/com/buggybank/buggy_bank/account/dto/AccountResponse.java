package com.buggybank.buggy_bank.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.buggybank.buggy_bank.account.entity.Account;
import com.buggybank.buggy_bank.account.entity.AccountType;
import com.buggybank.buggy_bank.common.money.Money;

public record AccountResponse(UUID id, String name, AccountType type, BigDecimal openingBalance) {

    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getType(), Money.toRupees(a.getOpeningBalancePaise()));
    }
}