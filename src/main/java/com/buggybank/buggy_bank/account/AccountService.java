package com.buggybank.buggy_bank.account;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.account.dto.AccountRequest;
import com.buggybank.buggy_bank.account.dto.AccountResponse;
import com.buggybank.buggy_bank.account.entity.Account;
import com.buggybank.buggy_bank.common.exception.BusinessRuleException;
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
        return toResponse(accounts.saveAndFlush(account));   // flush so the view can see the new row
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(UUID userId) {
        Map<UUID, Long> balances = accounts.balancesForUser(userId).stream()
                .collect(Collectors.toMap(AccountRepository.BalanceRow::getAccountId,
                                          AccountRepository.BalanceRow::getBalancePaise));
        return accounts.findByUserIdAndArchivedFalseOrderByCreatedAtAsc(userId).stream()
                .map(a -> AccountResponse.from(a, balances.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID id) {
        return toResponse(find(userId, id));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID id, AccountRequest req) {
        Account account = find(userId, id);
        String name = req.name().trim();
        if (!account.getName().equals(name) && accounts.existsByUserIdAndName(userId, name)) {
            throw new ConflictException("You already have an account with this name");
        }
        account.update(name, req.type(), Money.toPaise(req.openingBalance()));
        accounts.flush();   // push the new opening balance before reading the view
        return toResponse(account);
    }

    @Transactional
    public void archive(UUID userId, UUID id) {
        find(userId, id).archive();
    }

    @Transactional(readOnly = true)
    public void assertUsable(UUID userId, UUID accountId) {
        Account account = find(userId, accountId);   // 404 if it isn't this user's
        if (account.isArchived()) {
            throw new BusinessRuleException("Account is archived");
        }
    }

    private Account find(UUID userId, UUID id) {
        return accounts.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private AccountResponse toResponse(Account a) {
        return AccountResponse.from(a, accounts.balanceOf(a.getId()));
    }
}