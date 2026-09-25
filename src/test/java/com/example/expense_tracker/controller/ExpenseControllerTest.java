package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.CreateExpenseRequest;
import com.example.expense_tracker.dto.ExpenseResponse;
import com.example.expense_tracker.dto.UpdateExpenseRequest;
import com.example.expense_tracker.exception.ExpenseNotFoundException;
import com.example.expense_tracker.model.ExpenseCategory;
import com.example.expense_tracker.service.ExpenseService;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    private Authentication testAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "test@example.com",
                null,
                List.of()
        );
    }

    @Test
    void getExpenseByIdShouldReturn404WhenExpenseDoesNotExist() throws Exception {

        when(
                expenseService.getExpenseById(
                        999L,
                        "test@example.com"
                )
        ).thenThrow(
                new ExpenseNotFoundException(999L)
        );

        mockMvc.perform(
                        get("/expenses/999")
                                .principal(testAuthentication())
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Expense with id 999 not found")
                );
    }

    @Test
    void createExpenseShouldReturn201() throws Exception {

        ExpenseResponse response =
                new ExpenseResponse(
                        20L,
                        "Lunch",
                        new BigDecimal("15.50"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20)
                );

        when(
                expenseService.createExpense(
                        any(CreateExpenseRequest.class),
                        eq("test@example.com")
                )
        ).thenReturn(response);

        String json = """
        {
          "description": "Lunch",
          "amount": 15.50,
          "category": "FOOD",
          "date": "2026-09-20"
        }
        """;

        mockMvc.perform(
                        post("/expenses")
                                .principal(testAuthentication())
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(20)
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Lunch")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(15.50)
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("FOOD")
                )
                .andExpect(
                        jsonPath("$.date")
                                .value("2026-09-20")
                );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createExpenseShouldReturn400WhenRequestIsInvalid() throws Exception {

        String json = """
            {
              "description": "",
              "amount": -10.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        mockMvc.perform(
                        post("/expenses")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.errors.description")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.amount")
                                .exists()
                );

        verify(
                expenseService,
                never()
        ).createExpense(
                any(CreateExpenseRequest.class),
                anyString()
        );
    }

    @Test
    void deleteExpenseShouldReturn204() throws Exception {

        mockMvc.perform(
                        delete("/expenses/12")
                                .principal(testAuthentication())
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(expenseService)
                .deleteExpense(
                        12L,
                        "test@example.com"
                );
    }

    @Test
    void updateExpenseShouldReturnUpdatedExpense() throws Exception {

        ExpenseResponse response =
                new ExpenseResponse(
                        12L,
                        "Dinner",
                        new BigDecimal("25.00"),
                        ExpenseCategory.FOOD,
                        LocalDate.of(2026, 9, 20)
                );

        when(
                expenseService.updateExpense(
                        eq(12L),
                        any(UpdateExpenseRequest.class),
                        eq("test@example.com")
                )
        ).thenReturn(response);

        String json = """
        {
          "description": "Dinner",
          "amount": 25.00,
          "category": "FOOD",
          "date": "2026-09-20"
        }
        """;

        mockMvc.perform(
                        put("/expenses/12")
                                .principal(testAuthentication())
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(12)
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Dinner")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(25.00)
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("FOOD")
                )
                .andExpect(
                        jsonPath("$.date")
                                .value("2026-09-20")
                );

        verify(expenseService)
                .updateExpense(
                        eq(12L),
                        any(UpdateExpenseRequest.class),
                        eq("test@example.com")
                );
    }
}