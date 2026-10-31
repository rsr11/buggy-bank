package com.buggybank.buggy_bank.transaction;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.buggybank.buggy_bank.transaction.entity.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
}



// Create an account with openingBalance 1000. The response should show currentBalance: 1000.00.
// POST /api/transactions an EXPENSE of 250.75 with "occurredOn":"2026-10-28". Then GET /api/accounts should show 749.25.
// Add an INCOME of 500. The balance should be 1249.25.
// GET /api/transactions?size=1 should return one item, newest first, with totalPages of 2 or more.
// GET /api/transactions?from=2026-10-28&to=2026-10-28 should return only that day’s items.
// DELETE the expense. The balance should return to 1500.00.
// A 0 amount or a negative one should return 400.
// Log in as a second user. GET /api/transactions/<first user's id> should return 404, and POST with the first user’s accountId should return 404.
// Delete (archive) the account, then add a transaction to it. It should return 422 Account is archived.