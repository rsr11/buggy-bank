package com.buggybank.buggy_bank.wishlist.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlist_items")
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount_paise", nullable = false)
    private long targetAmountPaise;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishlistStatus status = WishlistStatus.ACTIVE;

    @Column(name = "achieved_at")
    private Instant achievedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WishlistItem() {}

    public WishlistItem(UUID userId, String name, long targetAmountPaise, LocalDate targetDate) {
        this.userId = userId;
        this.name = name;
        this.targetAmountPaise = targetAmountPaise;
        this.targetDate = targetDate;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String name, long targetAmountPaise, LocalDate targetDate) {
        this.name = name;
        this.targetAmountPaise = targetAmountPaise;
        this.targetDate = targetDate;
    }

    public void markAchieved() {
        this.status = WishlistStatus.ACHIEVED;
        this.achievedAt = Instant.now();
    }

    public void abandon() {
        this.status = WishlistStatus.ABANDONED;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public long getTargetAmountPaise() { return targetAmountPaise; }
    public LocalDate getTargetDate() { return targetDate; }
    public WishlistStatus getStatus() { return status; }
    public Instant getAchievedAt() { return achievedAt; }
    public Instant getCreatedAt() { return createdAt; }
}