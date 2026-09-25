package com.example.expense_tracker.dto;

public record LoginResponse(
        Long id,
        String email,
        String token
) {
}