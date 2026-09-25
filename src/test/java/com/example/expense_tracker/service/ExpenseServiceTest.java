package com.example.expense_tracker.service;

import com.example.expense_tracker.dto.CreateExpenseRequest;
import com.example.expense_tracker.dto.ExpenseResponse;
import com.example.expense_tracker.dto.UpdateExpenseRequest;
import com.example.expense_tracker.exception.ExpenseNotFoundException;
import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseCategory;
import com.example.expense_tracker.repository.AppUserRepository;
import com.example.expense_tracker.repository.ExpenseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    private ExpenseRepository expenseRepository;
    private AppUserRepository appUserRepository;
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {

        expenseRepository =
                mock(ExpenseRepository.class);

        appUserRepository =
                mock(AppUserRepository.class);

        expenseService =
                new ExpenseService(
                        expenseRepository,
                        appUserRepository
                );
    }

    @Test
    void getExpenseByIdShouldReturnExpense() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        Expense expense = new Expense(
                12L,
                "Coffee",
                new BigDecimal("4.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 18),
                user
        );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        12L,
                        user
                )
        ).thenReturn(
                Optional.of(expense)
        );

        var response =
                expenseService.getExpenseById(
                        12L,
                        "test@example.com"
                );

        assertEquals(
                12L,
                response.id()
        );

        assertEquals(
                "Coffee",
                response.description()
        );

        assertEquals(
                new BigDecimal("4.50"),
                response.amount()
        );

        assertEquals(
                ExpenseCategory.FOOD,
                response.category()
        );
    }

    @Test
    void getExpenseByIdShouldThrowWhenExpenseDoesNotExist() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        999L,
                        user
                )
        ).thenReturn(
                Optional.empty()
        );

        ExpenseNotFoundException exception =
                assertThrows(
                        ExpenseNotFoundException.class,
                        () -> expenseService.getExpenseById(
                                999L,
                                "test@example.com"
                        )
                );

        assertEquals(
                "Expense with id 999 not found",
                exception.getMessage()
        );
    }

    @Test
    void createExpenseShouldSaveExpenseForUser() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Supermarket",
                        new BigDecimal("42.30"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20)
                );

        Expense savedExpense = new Expense(
                5L,
                "Supermarket",
                new BigDecimal("42.30"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                user
        );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.save(
                        any(Expense.class)
                )
        ).thenReturn(
                savedExpense
        );

        var response =
                expenseService.createExpense(
                        request,
                        "test@example.com"
                );

        assertEquals(5L, response.id());
        assertEquals("Supermarket", response.description());
        assertEquals(
                new BigDecimal("42.30"),
                response.amount()
        );

        ArgumentCaptor<Expense> captor =
                ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository)
                .save(captor.capture());

        Expense expenseThatWasSaved =
                captor.getValue();

        assertEquals(
                user,
                expenseThatWasSaved.getUser()
        );
    }

    @Test
    void deleteExpenseShouldDeleteOwnedExpense() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        Expense expense = new Expense(
                12L,
                "Coffee",
                new BigDecimal("4.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 18),
                user
        );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        12L,
                        user
                )
        ).thenReturn(
                Optional.of(expense)
        );

        expenseService.deleteExpense(
                12L,
                "test@example.com"
        );

        verify(expenseRepository)
                .delete(expense);
    }

    @Test
    void deleteExpenseShouldThrowWhenExpenseDoesNotBelongToUser() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        999L,
                        user
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                ExpenseNotFoundException.class,
                () -> expenseService.deleteExpense(
                        999L,
                        "test@example.com"
                )
        );

        verify(
                expenseRepository,
                never()
        ).delete(any(Expense.class));
    }

    @Test
    void updateExpenseShouldUpdateOwnedExpense() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        Expense existingExpense = new Expense(
                12L,
                "Coffee",
                new BigDecimal("4.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 18),
                user
        );

        UpdateExpenseRequest request =
                new UpdateExpenseRequest(
                        "Dinner",
                        new BigDecimal("25.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20)
                );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        12L,
                        user
                )
        ).thenReturn(
                Optional.of(existingExpense)
        );

        when(
                expenseRepository.save(existingExpense)
        ).thenReturn(
                existingExpense
        );

        var response =
                expenseService.updateExpense(
                        12L,
                        request,
                        "test@example.com"
                );

        assertEquals(
                "Dinner",
                response.description()
        );

        assertEquals(
                new BigDecimal("25.00"),
                response.amount()
        );

        assertEquals(
                ExpenseCategory.FOOD,
                response.category()
        );

        assertEquals(
                LocalDate.of(2026, 9, 20),
                response.date()
        );

        verify(expenseRepository)
                .save(existingExpense);
    }

    @Test
    void updateExpenseShouldThrowWhenExpenseDoesNotBelongToUser() {

        AppUser user = new AppUser(
                2L,
                "test@example.com",
                "hashed-password"
        );

        UpdateExpenseRequest request =
                new UpdateExpenseRequest(
                        "Hacked",
                        new BigDecimal("99.99"),
                        ExpenseCategory.OTHER,
                        LocalDate.of(2026, 9, 20)
                );

        when(
                appUserRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                expenseRepository.findByIdAndUser(
                        999L,
                        user
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                ExpenseNotFoundException.class,
                () -> expenseService.updateExpense(
                        999L,
                        request,
                        "test@example.com"
                )
        );

        verify(
                expenseRepository,
                never()
        ).save(any(Expense.class));
    }

}