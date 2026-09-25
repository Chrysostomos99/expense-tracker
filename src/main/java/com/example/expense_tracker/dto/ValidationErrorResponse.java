package com.example.expense_tracker.dto;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        Map<String, String> errors
) {
}