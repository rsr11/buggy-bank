package com.buggybank.buggy_bank.wishlist;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.buggybank.buggy_bank.common.dto.PageResponse;
import com.buggybank.buggy_bank.common.security.CurrentUser;
import com.buggybank.buggy_bank.wishlist.dto.PiggyAmountRequest;
import com.buggybank.buggy_bank.wishlist.dto.PiggyEntryResponse;
import com.buggybank.buggy_bank.wishlist.dto.WishlistItemRequest;
import com.buggybank.buggy_bank.wishlist.dto.WishlistItemResponse;
import com.buggybank.buggy_bank.wishlist.entity.WishlistStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody WishlistItemRequest req) {
        return service.create(CurrentUser.id(jwt), req);
    }

    @GetMapping
    public List<WishlistItemResponse> list(@AuthenticationPrincipal Jwt jwt,
                                           @RequestParam(defaultValue = "ACTIVE") WishlistStatus status) {
        return service.list(CurrentUser.id(jwt), status);
    }

    @GetMapping("/{id}")
    public WishlistItemResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.get(CurrentUser.id(jwt), id);
    }

    @PutMapping("/{id}")
    public WishlistItemResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                       @Valid @RequestBody WishlistItemRequest req) {
        return service.update(CurrentUser.id(jwt), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abandon(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.abandon(CurrentUser.id(jwt), id);
    }

    @PostMapping("/{id}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse deposit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                        @Valid @RequestBody PiggyAmountRequest req) {
        return service.deposit(CurrentUser.id(jwt), id, req);
    }

    @PostMapping("/{id}/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse withdraw(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                         @Valid @RequestBody PiggyAmountRequest req) {
        return service.withdraw(CurrentUser.id(jwt), id, req);
    }

    @PostMapping("/{id}/achieve")
    public WishlistItemResponse achieve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.achieve(CurrentUser.id(jwt), id);
    }

    @GetMapping("/{id}/entries")
    public PageResponse<PiggyEntryResponse> history(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.history(CurrentUser.id(jwt), id, page, size);
    }
}