package com.example.expense_tracker.repository;

import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseCategory;

import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUserShouldReturnOnlyUsersExpenses() {

        AppUser userA = new AppUser(
                null,
                "userA@example.com",
                "hashA"
        );

        AppUser userB = new AppUser(
                null,
                "userB@example.com",
                "hashB"
        );

        userA = appUserRepository.save(userA);
        userB = appUserRepository.save(userB);

        Expense expenseA1 = new Expense(
                null,
                "Coffee A",
                new BigDecimal("4.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                userA
        );

        Expense expenseA2 = new Expense(
                null,
                "Fuel A",
                new BigDecimal("30.00"),
                ExpenseCategory.TRANSPORT,
                LocalDate.of(2026, 9, 20),
                userA
        );

        Expense expenseB = new Expense(
                null,
                "Dinner B",
                new BigDecimal("20.00"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                userB
        );

        expenseRepository.save(expenseA1);
        expenseRepository.save(expenseA2);
        expenseRepository.save(expenseB);

        Page<Expense> result =
                expenseRepository.findByUser(
                        userA,
                        PageRequest.of(0, 10)
                );

        assertEquals(
                2,
                result.getTotalElements()
        );

        assertEquals(
                "Coffee A",
                result.getContent().get(0).getDescription()
        );

        assertEquals(
                "Fuel A",
                result.getContent().get(1).getDescription()
        );
    }

    @Test
    void findByIdAndUserShouldRespectOwnership() {

        AppUser owner = new AppUser(
                null,
                "owner@example.com",
                "hash-owner"
        );

        AppUser otherUser = new AppUser(
                null,
                "other@example.com",
                "hash-other"
        );

        owner = appUserRepository.save(owner);
        otherUser = appUserRepository.save(otherUser);

        Expense expense = new Expense(
                null,
                "Owner Coffee",
                new BigDecimal("5.00"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                owner
        );

        expense = expenseRepository.save(expense);

        var ownerResult =
                expenseRepository.findByIdAndUser(
                        expense.getId(),
                        owner
                );

        var otherUserResult =
                expenseRepository.findByIdAndUser(
                        expense.getId(),
                        otherUser
                );

        assertTrue(ownerResult.isPresent());

        assertTrue(otherUserResult.isEmpty());
    }

    @Test
    void findByUserAndCategoryShouldReturnOnlyMatchingUsersExpenses() {

        AppUser userA = new AppUser(
                null,
                "userA2@example.com",
                "hashA"
        );

        AppUser userB = new AppUser(
                null,
                "userB2@example.com",
                "hashB"
        );

        userA = appUserRepository.save(userA);
        userB = appUserRepository.save(userB);

        Expense coffeeA = new Expense(
                null,
                "Coffee A",
                new BigDecimal("4.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                userA
        );

        Expense fuelA = new Expense(
                null,
                "Fuel A",
                new BigDecimal("30.00"),
                ExpenseCategory.TRANSPORT,
                LocalDate.of(2026, 9, 20),
                userA
        );

        Expense dinnerB = new Expense(
                null,
                "Dinner B",
                new BigDecimal("20.00"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 9, 20),
                userB
        );

        expenseRepository.save(coffeeA);
        expenseRepository.save(fuelA);
        expenseRepository.save(dinnerB);

        Page<Expense> result =
                expenseRepository.findByUserAndCategory(
                        userA,
                        ExpenseCategory.FOOD,
                        PageRequest.of(0, 10)
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Coffee A",
                result.getContent().get(0).getDescription()
        );
    }

    @Test
    void getTotalAmountByUserShouldSumOnlyUsersExpenses() {

        AppUser userA = new AppUser(
                null,
                "statsA@example.com",
                "hashA"
        );

        AppUser userB = new AppUser(
                null,
                "statsB@example.com",
                "hashB"
        );

        userA = appUserRepository.save(userA);
        userB = appUserRepository.save(userB);

        expenseRepository.save(
                new Expense(
                        null,
                        "Coffee A",
                        new BigDecimal("5.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20),
                        userA
                )
        );

        expenseRepository.save(
                new Expense(
                        null,
                        "Fuel A",
                        new BigDecimal("30.00"),
                        ExpenseCategory.TRANSPORT,
                        LocalDate.of(2026, 9, 20),
                        userA
                )
        );

        expenseRepository.save(
                new Expense(
                        null,
                        "Dinner B",
                        new BigDecimal("100.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20),
                        userB
                )
        );

        BigDecimal total =
                expenseRepository.getTotalAmountByUser(userA);

        assertEquals(
                new BigDecimal("35.00"),
                total
        );
    }

    @Test
    void getTotalsByCategoryByUserShouldGroupOnlyUsersExpenses() {

        AppUser userA = new AppUser(
                null,
                "groupA@example.com",
                "hashA"
        );

        AppUser userB = new AppUser(
                null,
                "groupB@example.com",
                "hashB"
        );

        userA = appUserRepository.save(userA);
        userB = appUserRepository.save(userB);

        expenseRepository.save(
                new Expense(
                        null,
                        "Coffee A",
                        new BigDecimal("5.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20),
                        userA
                )
        );

        expenseRepository.save(
                new Expense(
                        null,
                        "Lunch A",
                        new BigDecimal("15.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20),
                        userA
                )
        );

        expenseRepository.save(
                new Expense(
                        null,
                        "Fuel A",
                        new BigDecimal("30.00"),
                        ExpenseCategory.TRANSPORT,
                        LocalDate.of(2026, 9, 20),
                        userA
                )
        );

        expenseRepository.save(
                new Expense(
                        null,
                        "Dinner B",
                        new BigDecimal("100.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20),
                        userB
                )
        );

        List<Object[]> result =
                expenseRepository.getTotalsByCategoryByUser(userA);

        assertEquals(
                2,
                result.size()
        );

        BigDecimal foodTotal = result.stream()
                .filter(row -> row[0] == ExpenseCategory.FOOD)
                .map(row -> (BigDecimal) row[1])
                .findFirst()
                .orElseThrow();

        BigDecimal transportTotal = result.stream()
                .filter(row -> row[0] == ExpenseCategory.TRANSPORT)
                .map(row -> (BigDecimal) row[1])
                .findFirst()
                .orElseThrow();

        assertEquals(
                new BigDecimal("20.00"),
                foodTotal
        );

        assertEquals(
                new BigDecimal("30.00"),
                transportTotal
        );
    }
}