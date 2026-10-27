package com.buggybank.buggy_bank.auth.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}