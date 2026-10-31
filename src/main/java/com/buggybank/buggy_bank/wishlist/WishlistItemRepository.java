package com.buggybank.buggy_bank.wishlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.buggybank.buggy_bank.wishlist.entity.WishlistItem;
import com.buggybank.buggy_bank.wishlist.entity.WishlistStatus;

import jakarta.persistence.LockModeType;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    interface SavedRow {
        UUID getItemId();
        long getSavedPaise();
    }

    Optional<WishlistItem> findByIdAndUserId(UUID id, UUID userId);

    List<WishlistItem> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, WishlistStatus status);

    // SELECT ... FOR UPDATE: anyone else touching this row waits until our transaction ends
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WishlistItem w where w.id = :id and w.userId = :userId")
    Optional<WishlistItem> findForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(value = """
            select item_id as "itemId", cast(saved_paise as bigint) as "savedPaise"
            from piggy_balances where user_id = :userId
            """, nativeQuery = true)
    List<SavedRow> savedForUser(@Param("userId") UUID userId);

    @Query(value = "select cast(saved_paise as bigint) from piggy_balances where item_id = :itemId",
           nativeQuery = true)
    long savedOf(@Param("itemId") UUID itemId);
}