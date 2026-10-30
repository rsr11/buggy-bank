package com.buggybank.buggy_bank.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buggybank.buggy_bank.account.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserIdAndArchivedFalseOrderByCreatedAtAsc(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}