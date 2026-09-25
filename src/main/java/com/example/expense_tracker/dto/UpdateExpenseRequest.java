package com.example.expense_tracker.dto;

import com.example.expense_tracker.model.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(

        @NotBlank(message = "Description is required")
        @Size(max = 100, message = "Description must be at most 100 characters")
        String description,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        ExpenseCategory category,

        @NotNull(message = "Date is required")
        LocalDate date

) {
}