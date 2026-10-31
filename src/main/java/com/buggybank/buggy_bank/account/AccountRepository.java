package com.buggybank.buggy_bank.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.buggybank.buggy_bank.account.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserIdAndArchivedFalseOrderByCreatedAtAsc(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);

    interface BalanceRow {
    UUID getAccountId();
    long getBalancePaise();
}

@Query(value = """
        select account_id as "accountId", cast(balance_paise as bigint) as "balancePaise"
        from account_balances where user_id = :userId
        """, nativeQuery = true)
List<BalanceRow> balancesForUser(@Param("userId") UUID userId);

@Query(value = "select cast(balance_paise as bigint) from account_balances where account_id = :accountId",
       nativeQuery = true)
long balanceOf(@Param("accountId") UUID accountId);

}

