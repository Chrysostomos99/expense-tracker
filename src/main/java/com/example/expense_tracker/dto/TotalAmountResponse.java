package com.example.expense_tracker.dto;

import java.math.BigDecimal;

public record TotalAmountResponse(
        BigDecimal totalAmount
) {
}