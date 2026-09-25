package com.example.expense_tracker.dto;

public record ApiErrorResponse(
        int status,
        String message
) {
}