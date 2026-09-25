package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.*;
import com.example.expense_tracker.model.ExpenseCategory;
import com.example.expense_tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public Page<ExpenseResponse> getExpenses(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return expenseService.getExpensesPaged(
                email,
                category,
                from,
                to,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    public ExpenseResponse getExpenseById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return expenseService.getExpenseById(
                id,
                email
        );
    }

    @GetMapping("/stats/total")
    public TotalAmountResponse getTotalAmount(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return expenseService.getTotalAmount(
                email,
                from,
                to
        );
    }

    @GetMapping("/stats/by-category")
    public List<CategoryTotalResponse> getTotalsByCategory(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return expenseService.getTotalsByCategory(
                email,
                from,
                to
        );
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        ExpenseResponse response =
                expenseService.createExpense(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        expenseService.deleteExpense(
                id,
                email
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return expenseService.updateExpense(
                id,
                request,
                email
        );
    }
}