package com.buggybank.buggy_bank.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String timezone = "Asia/Kolkata";

    @JdbcTypeCode(SqlTypes.CHAR)   // the column is char(3); without this, schema validation fails
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "streak_period", nullable = false)
    private String streakPeriod = "WEEK";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected User() {}   // JPA needs this

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getTimezone() { return timezone; }
    public String getCurrency() { return currency; }
    public String getStreakPeriod() { return streakPeriod; }
    public Instant getCreatedAt() { return createdAt; }
}