package com.buggybank.buggy_bank.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.buggybank.buggy_bank.common.exception.NotFoundException;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
public class PingController {

    @GetMapping("/api/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }

    public record EchoRequest(@NotBlank String name) {}

@PostMapping("/api/ping/echo")
public Map<String, String> echo(@Valid @RequestBody EchoRequest request) {
    return Map.of("hello", request.name());
}

@GetMapping("/api/ping/missing")
public void missing() {
    throw new NotFoundException("Nothing here");
}
}

