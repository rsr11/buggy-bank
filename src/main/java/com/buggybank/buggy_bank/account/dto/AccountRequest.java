package com.buggybank.buggy_bank.account.dto;

import java.math.BigDecimal;

import com.buggybank.buggy_bank.account.entity.AccountType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank @Size(max = 60) String name,
        @NotNull AccountType type,
        @NotNull @Digits(integer = 12, fraction = 2) BigDecimal openingBalance) {}