package com.buggybank.buggy_bank.account.entity;

import java.time.Instant;
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
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "opening_balance_paise", nullable = false)
    private long openingBalancePaise;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {}

    public Account(UUID userId, String name, AccountType type, long openingBalancePaise) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.openingBalancePaise = openingBalancePaise;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String name, AccountType type, long openingBalancePaise) {
        this.name = name;
        this.type = type;
        this.openingBalancePaise = openingBalancePaise;
    }

    public void archive() {
        this.archived = true;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public AccountType getType() { return type; }
    public long getOpeningBalancePaise() { return openingBalancePaise; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
}