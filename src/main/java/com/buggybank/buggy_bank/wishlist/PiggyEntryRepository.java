package com.buggybank.buggy_bank.wishlist;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.buggybank.buggy_bank.wishlist.entity.PiggyEntry;

public interface PiggyEntryRepository extends JpaRepository<PiggyEntry, UUID> {
    Page<PiggyEntry> findByItemIdAndUserId(UUID itemId, UUID userId, Pageable pageable);
}