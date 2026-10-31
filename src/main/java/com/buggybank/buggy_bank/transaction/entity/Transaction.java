package com.buggybank.buggy_bank.transaction.entity;

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
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "occurred_on", nullable = false)
    private LocalDate occurredOn;

    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(UUID userId, UUID accountId, TransactionType type,
                       long amountPaise, LocalDate occurredOn, String note) {
        this.userId = userId;
        this.accountId = accountId;
        this.type = type;
        this.amountPaise = amountPaise;
        this.occurredOn = occurredOn;
        this.note = note;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public long getAmountPaise() { return amountPaise; }
    public LocalDate getOccurredOn() { return occurredOn; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}