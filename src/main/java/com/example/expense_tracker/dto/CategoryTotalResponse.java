package com.example.expense_tracker.dto;

import com.example.expense_tracker.model.ExpenseCategory;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        ExpenseCategory category,
        BigDecimal totalAmount
) {
}