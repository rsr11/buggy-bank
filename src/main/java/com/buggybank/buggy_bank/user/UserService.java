package com.buggybank.buggy_bank.user;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.common.exception.NotFoundException;
import com.buggybank.buggy_bank.user.dto.UserResponse;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return users.findById(id).map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}