package com.buggybank.buggy_bank.user.dto;

import java.util.UUID;

import com.buggybank.buggy_bank.user.entity.User;

public record UserResponse(UUID id, String email, String displayName, String timezone) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getTimezone());
    }
}