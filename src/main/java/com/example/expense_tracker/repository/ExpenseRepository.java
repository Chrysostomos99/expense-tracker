package com.example.expense_tracker.repository;

import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndUser(
            Long id,
            AppUser user
    );
    Page<Expense> findByUser(
            AppUser user,
            Pageable pageable
    );
    Page<Expense> findByUserAndCategory(
            AppUser user,
            ExpenseCategory category,
            Pageable pageable
    );

    Page<Expense> findByUserAndDateBetween(
            AppUser user,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    Page<Expense> findByUserAndCategoryAndDateBetween(
            AppUser user,
            ExpenseCategory category,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );




    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user = :user
    """)
    BigDecimal getTotalAmountByUser(
            @Param("user") AppUser user
    );


    @Query("""
    SELECT e.category, SUM(e.amount)
    FROM Expense e
    WHERE e.user = :user
    GROUP BY e.category
    """)
    List<Object[]> getTotalsByCategoryByUser(
            @Param("user") AppUser user
    );


    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user = :user
      AND e.date BETWEEN :from AND :to
    """)
    BigDecimal getTotalAmountBetweenByUser(
            @Param("user") AppUser user,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );


    @Query("""
    SELECT e.category, SUM(e.amount)
    FROM Expense e
    WHERE e.user = :user
      AND e.date BETWEEN :from AND :to
    GROUP BY e.category
    """)
    List<Object[]> getTotalsByCategoryBetweenByUser(
            @Param("user") AppUser user,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );


}

