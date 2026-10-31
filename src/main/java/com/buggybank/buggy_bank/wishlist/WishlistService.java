package com.buggybank.buggy_bank.wishlist;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.common.dto.PageResponse;
import com.buggybank.buggy_bank.common.exception.BusinessRuleException;
import com.buggybank.buggy_bank.common.exception.NotFoundException;
import com.buggybank.buggy_bank.common.money.Money;
import com.buggybank.buggy_bank.common.time.UserTime;
import com.buggybank.buggy_bank.user.UserService;
import com.buggybank.buggy_bank.wishlist.dto.PiggyAmountRequest;
import com.buggybank.buggy_bank.wishlist.dto.PiggyEntryResponse;
import com.buggybank.buggy_bank.wishlist.dto.WishlistItemRequest;
import com.buggybank.buggy_bank.wishlist.dto.WishlistItemResponse;
import com.buggybank.buggy_bank.wishlist.entity.PiggyEntry;
import com.buggybank.buggy_bank.wishlist.entity.PiggyEntryType;
import com.buggybank.buggy_bank.wishlist.entity.WishlistItem;
import com.buggybank.buggy_bank.wishlist.entity.WishlistStatus;

@Service
public class WishlistService {

    private final WishlistItemRepository items;
    private final PiggyEntryRepository entries;
    private final UserService userService;

    public WishlistService(WishlistItemRepository items, PiggyEntryRepository entries, UserService userService) {
        this.items = items;
        this.entries = entries;
        this.userService = userService;
    }

    @Transactional
    public WishlistItemResponse create(UUID userId, WishlistItemRequest req) {
        LocalDate today = today(userId);
        checkTargetDate(req.targetDate(), today);
        WishlistItem item = new WishlistItem(userId, req.name().trim(),
                Money.toPaise(req.targetAmount()), req.targetDate());
        items.saveAndFlush(item);
        return toResponse(item, 0, today);
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(UUID userId, WishlistStatus status) {
        Map<UUID, Long> saved = items.savedForUser(userId).stream()
                .collect(Collectors.toMap(WishlistItemRepository.SavedRow::getItemId,
                                          WishlistItemRepository.SavedRow::getSavedPaise));
        LocalDate today = today(userId);
        return items.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status).stream()
                .map(i -> toResponse(i, saved.getOrDefault(i.getId(), 0L), today))
                .toList();
    }

    @Transactional(readOnly = true)
    public WishlistItemResponse get(UUID userId, UUID id) {
        WishlistItem item = find(userId, id);
        return toResponse(item, items.savedOf(id), today(userId));
    }

    @Transactional
    public WishlistItemResponse update(UUID userId, UUID id, WishlistItemRequest req) {
        WishlistItem item = find(userId, id);
        requireActive(item);
        LocalDate today = today(userId);
        checkTargetDate(req.targetDate(), today);
        item.update(req.name().trim(), Money.toPaise(req.targetAmount()), req.targetDate());
        items.flush();
        return toResponse(item, items.savedOf(id), today);
    }

    /** "Delete" abandons the item. The ledger stays, but the money stops counting as earmarked. */
    @Transactional
    public void abandon(UUID userId, UUID id) {
        WishlistItem item = find(userId, id);
        requireActive(item);
        item.abandon();
    }

    @Transactional
    public WishlistItemResponse deposit(UUID userId, UUID id, PiggyAmountRequest req) {
        return move(userId, id, req, PiggyEntryType.DEPOSIT);
    }

    @Transactional
    public WishlistItemResponse withdraw(UUID userId, UUID id, PiggyAmountRequest req) {
        return move(userId, id, req, PiggyEntryType.WITHDRAWAL);
    }

    @Transactional
    public WishlistItemResponse achieve(UUID userId, UUID id) {
        WishlistItem item = items.findForUpdate(id, userId)
                .orElseThrow(() -> new NotFoundException("Wishlist item not found"));
        requireActive(item);
        long saved = items.savedOf(id);
        if (saved < item.getTargetAmountPaise()) {
            throw new BusinessRuleException("The piggy has not reached the target yet");
        }
        item.markAchieved();
        return toResponse(item, saved, today(userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<PiggyEntryResponse> history(UUID userId, UUID id, int page, int size) {
        find(userId, id);   // 404 if it isn't this user's item
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "entryDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(entries.findByItemIdAndUserId(id, userId, pageable).map(PiggyEntryResponse::from));
    }

    // ---- the important method ----
    private WishlistItemResponse move(UUID userId, UUID id, PiggyAmountRequest req, PiggyEntryType type) {
        // 1. Lock the item row. A second request for the same piggy waits here until we commit.
        WishlistItem item = items.findForUpdate(id, userId)
                .orElseThrow(() -> new NotFoundException("Wishlist item not found"));
        requireActive(item);

        // 2. Read the balance while holding the lock, so nobody can change it under us.
        long amount = Money.toPaise(req.amount());
        long saved = items.savedOf(id);
        if (type == PiggyEntryType.WITHDRAWAL && amount > saved) {
            throw new BusinessRuleException("Not enough saved in this piggy");
        }

        // 3. Write the ledger entry, dated in the USER's timezone (streaks will read this).
        LocalDate today = today(userId);
        entries.saveAndFlush(new PiggyEntry(id, userId, type, amount, today, req.note()));

        long newSaved = type == PiggyEntryType.DEPOSIT ? saved + amount : saved - amount;
        return toResponse(item, newSaved, today);
    }

    private WishlistItem find(UUID userId, UUID id) {
        return items.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Wishlist item not found"));
    }

    private void requireActive(WishlistItem item) {
        if (item.getStatus() != WishlistStatus.ACTIVE) {
            throw new BusinessRuleException("This wishlist item is not active");
        }
    }

    private LocalDate today(UUID userId) {
        return UserTime.today(userService.zoneOf(userId));
    }

    private void checkTargetDate(LocalDate targetDate, LocalDate today) {
        if (targetDate != null && targetDate.isBefore(today)) {
            throw new BusinessRuleException("Target date is in the past");
        }
    }

    private WishlistItemResponse toResponse(WishlistItem i, long savedPaise, LocalDate today) {
        int progress = (int) Math.min(100, savedPaise * 100 / i.getTargetAmountPaise());
        Long weekly = PaceCalculator.weeklyNeededPaise(i.getTargetAmountPaise(), savedPaise, today, i.getTargetDate());
        Long daysLeft = i.getTargetDate() == null ? null : ChronoUnit.DAYS.between(today, i.getTargetDate());
        return new WishlistItemResponse(i.getId(), i.getName(), Money.toRupees(i.getTargetAmountPaise()),
                i.getTargetDate(), i.getStatus(), Money.toRupees(savedPaise), progress,
                weekly == null ? null : Money.toRupees(weekly), daysLeft);
    }
}