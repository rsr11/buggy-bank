package com.buggybank.buggy_bank.account;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.account.dto.AccountRequest;
import com.buggybank.buggy_bank.account.dto.AccountResponse;
import com.buggybank.buggy_bank.account.entity.Account;
import com.buggybank.buggy_bank.common.exception.ConflictException;
import com.buggybank.buggy_bank.common.exception.NotFoundException;
import com.buggybank.buggy_bank.common.money.Money;

@Service
public class AccountService {

    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public AccountResponse create(UUID userId, AccountRequest req) {
        String name = req.name().trim();
        if (accounts.existsByUserIdAndName(userId, name)) {
            throw new ConflictException("You already have an account with this name");
        }
        Account account = new Account(userId, name, req.type(), Money.toPaise(req.openingBalance()));
        return AccountResponse.from(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(UUID userId) {
        return accounts.findByUserIdAndArchivedFalseOrderByCreatedAtAsc(userId)
                .stream().map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID id) {
        return AccountResponse.from(find(userId, id));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID id, AccountRequest req) {
        Account account = find(userId, id);
        String name = req.name().trim();
        if (!account.getName().equals(name) && accounts.existsByUserIdAndName(userId, name)) {
            throw new ConflictException("You already have an account with this name");
        }
        account.update(name, req.type(), Money.toPaise(req.openingBalance()));
        return AccountResponse.from(account);   // no save() call: see note below
    }

    @Transactional
    public void archive(UUID userId, UUID id) {
        find(userId, id).archive();
    }

    private Account find(UUID userId, UUID id) {
        return accounts.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
}