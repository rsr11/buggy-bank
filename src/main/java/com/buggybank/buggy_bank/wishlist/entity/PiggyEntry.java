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
@Table(name = "piggy_entries")
public class PiggyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PiggyEntryType type;

    @Column(name = "amount_paise", nullable = false, updatable = false)
    private long amountPaise;

    @Column(name = "entry_date", nullable = false, updatable = false)
    private LocalDate entryDate;

    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PiggyEntry() {}

    public PiggyEntry(UUID itemId, UUID userId, PiggyEntryType type, long amountPaise,
                      LocalDate entryDate, String note) {
        this.itemId = itemId;
        this.userId = userId;
        this.type = type;
        this.amountPaise = amountPaise;
        this.entryDate = entryDate;
        this.note = note;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public PiggyEntryType getType() { return type; }
    public long getAmountPaise() { return amountPaise; }
    public LocalDate getEntryDate() { return entryDate; }
    public String getNote() { return note; }
}