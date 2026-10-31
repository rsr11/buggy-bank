package com.buggybank.buggy_bank.transaction;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.account.AccountService;
import com.buggybank.buggy_bank.common.dto.PageResponse;
import com.buggybank.buggy_bank.common.exception.BusinessRuleException;
import com.buggybank.buggy_bank.common.exception.NotFoundException;
import com.buggybank.buggy_bank.common.money.Money;
import com.buggybank.buggy_bank.transaction.dto.TransactionRequest;
import com.buggybank.buggy_bank.transaction.dto.TransactionResponse;
import com.buggybank.buggy_bank.transaction.entity.Transaction;

@Service
public class TransactionService {

    private final TransactionRepository transactions;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactions, AccountService accountService) {
        this.transactions = transactions;
        this.accountService = accountService;
    }

    @Transactional
    public TransactionResponse create(UUID userId, TransactionRequest req) {
        accountService.assertUsable(userId, req.accountId());
        Transaction t = new Transaction(userId, req.accountId(), req.type(),
                Money.toPaise(req.amount()), req.occurredOn(), req.note());
        return TransactionResponse.from(transactions.save(t));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> list(UUID userId, UUID accountId,
                                                  LocalDate from, LocalDate to, int page, int size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessRuleException("'from' must not be after 'to'");
        }
        Specification<Transaction> spec = (root, q, cb) -> cb.equal(root.get("userId"), userId);
        if (accountId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("accountId"), accountId));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("occurredOn"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("occurredOn"), to));
        }
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "occurredOn").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<Transaction> result = transactions.findAll(spec, pageable);
        return PageResponse.from(result.map(TransactionResponse::from));
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(UUID userId, UUID id) {
        return TransactionResponse.from(find(userId, id));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        transactions.delete(find(userId, id));
    }

    private Transaction find(UUID userId, UUID id) {
        return transactions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
    }
}