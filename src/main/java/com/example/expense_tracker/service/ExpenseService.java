package com.example.expense_tracker.service;

import com.example.expense_tracker.dto.*;
import com.example.expense_tracker.exception.ExpenseNotFoundException;
import com.example.expense_tracker.exception.InvalidQueryParameterException;
import com.example.expense_tracker.exception.UserNotFoundException;
import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseCategory;
import com.example.expense_tracker.repository.AppUserRepository;
import com.example.expense_tracker.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import com.example.expense_tracker.exception.InvalidDateRangeException;

@Service
public class ExpenseService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "description",
            "amount",
            "category",
            "date"
    );

    private final ExpenseRepository expenseRepository;
    private final AppUserRepository appUserRepository;

    public ExpenseService(ExpenseRepository expenseRepository, AppUserRepository appUserRepository) {
        this.expenseRepository = expenseRepository;
        this.appUserRepository = appUserRepository;
    }

    public ExpenseResponse getExpenseById(Long id, String email) {

        AppUser user = findUserByEmail(email);

        Expense expense = findExpenseByIdAndUser(id, user);

        return toResponse(expense);
    }

    public ExpenseResponse createExpense(CreateExpenseRequest request, String email) {

        AppUser user = findUserByEmail(email);

        Expense expense = new Expense(
                null,
                request.description(),
                request.amount(),
                request.category(),
                request.date(),
                user
        );

        Expense savedExpense = expenseRepository.save(expense);

        return toResponse(savedExpense);
    }

    public void deleteExpense(Long id, String email) {

        AppUser user = findUserByEmail(email);

        Expense expense = findExpenseByIdAndUser(id, user);

        expenseRepository.delete(expense);
    }

    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request, String email) {

        AppUser user = findUserByEmail(email);

        Expense existingExpense =
                findExpenseByIdAndUser(id, user);

        existingExpense.setDescription(request.description());
        existingExpense.setAmount(request.amount());
        existingExpense.setCategory(request.category());
        existingExpense.setDate(request.date());

        Expense savedExpense =
                expenseRepository.save(existingExpense);

        return toResponse(savedExpense);
    }

    public Page<ExpenseResponse> getExpensesPaged(String email, ExpenseCategory category, LocalDate from, LocalDate to, int page, int size, String sortBy, String direction) {

        AppUser user = findUserByEmail(email);

        validateDateRange(from, to);
        validatePagingAndSorting(page, size, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Expense> expenses = findExpenses(
                user,
                category,
                from,
                to,
                pageable
        );

        return expenses.map(this::toResponse);
    }

    public TotalAmountResponse getTotalAmount(String email, LocalDate from, LocalDate to) {

        AppUser user = findUserByEmail(email);

        validateDateRange(from, to);

        BigDecimal totalAmount;

        if (from != null && to != null) {

            totalAmount =
                    expenseRepository
                            .getTotalAmountBetweenByUser(
                                    user,
                                    from,
                                    to
                            );

        } else {

            totalAmount =
                    expenseRepository
                            .getTotalAmountByUser(user);
        }

        return new TotalAmountResponse(totalAmount);
    }

    public List<CategoryTotalResponse> getTotalsByCategory(String email, LocalDate from, LocalDate to) {

        AppUser user = findUserByEmail(email);

        validateDateRange(from, to);

        List<Object[]> rows;

        if (from != null && to != null) {

            rows =
                    expenseRepository
                            .getTotalsByCategoryBetweenByUser(
                                    user,
                                    from,
                                    to
                            );

        } else {

            rows =
                    expenseRepository
                            .getTotalsByCategoryByUser(user);
        }

        return rows.stream()
                .map(row -> new CategoryTotalResponse(
                        (ExpenseCategory) row[0],
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    private AppUser findUserByEmail(String email) {

        return appUserRepository
                .findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private Expense findExpenseByIdAndUser(Long id, AppUser user) {

        return expenseRepository
                .findByIdAndUser(id, user)
                .orElseThrow(
                        () -> new ExpenseNotFoundException(id)
                );
    }

    private void validatePagingAndSorting(int page, int size, String sortBy, String direction) {

        if (page < 0) {
            throw new InvalidQueryParameterException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size <= 0 || size > 100) {
            throw new InvalidQueryParameterException(
                    "Size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidQueryParameterException(
                    "Invalid sort field: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new InvalidQueryParameterException(
                    "Direction must be 'asc' or 'desc'"
            );
        }
    }

    private Page<Expense> findExpenses(AppUser user, ExpenseCategory category, LocalDate from, LocalDate to, Pageable pageable) {

        if (category != null && from != null) {

            return expenseRepository
                    .findByUserAndCategoryAndDateBetween(
                            user,
                            category,
                            from,
                            to,
                            pageable
                    );
        }

        if (category != null) {

            return expenseRepository
                    .findByUserAndCategory(
                            user,
                            category,
                            pageable
                    );
        }

        if (from != null) {

            return expenseRepository
                    .findByUserAndDateBetween(
                            user,
                            from,
                            to,
                            pageable
                    );
        }

        return expenseRepository.findByUser(user, pageable);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {

        if ((from == null) != (to == null)) {
            throw new InvalidDateRangeException(
                    "Both 'from' and 'to' dates must be provided together"
            );
        }

        if (from != null && from.isAfter(to)) {
            throw new InvalidDateRangeException(
                    "'from' date must be before or equal to 'to' date"
            );
        }
    }

    private ExpenseResponse toResponse(Expense expense) {

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }
}