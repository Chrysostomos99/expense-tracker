package com.example.expense_tracker.dto;

import com.example.expense_tracker.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate date
) {
}