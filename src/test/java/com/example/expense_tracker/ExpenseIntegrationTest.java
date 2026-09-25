package com.example.expense_tracker;

import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.repository.AppUserRepository;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtService jwtService;

    private AppUser user;
    private String token;

    @BeforeEach
    void setUp() {

        expenseRepository.deleteAll();
        appUserRepository.deleteAll();

        user = appUserRepository.save(
                new AppUser(
                        null,
                        "test@example.com",
                        "fake-hash"
                )
        );

        token =
                jwtService.generateToken(
                        user.getEmail()
                );
    }

    @Test
    void createAndGetExpenseShouldWorkEndToEnd() throws Exception {

        String json = """
            {
              "description": "Integration Coffee",
              "amount": 4.50,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Integration Coffee")
                );

        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                org.springframework.data.domain.PageRequest.of(
                                        0,
                                        10
                                )
                        )
                        .getContent()
                        .get(0)
                        .getId();

        mockMvc.perform(
                        get("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(expenseId)
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Integration Coffee")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(4.50)
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("FOOD")
                );
    }

    @Test
    void userShouldNotAccessAnotherUsersExpense() throws Exception {

        AppUser otherUser = appUserRepository.save(
                new AppUser(
                        null,
                        "other@example.com",
                        "fake-hash"
                )
        );

        String otherToken =
                jwtService.generateToken(
                        otherUser.getEmail()
                );

        String json = """
            {
              "description": "Private Coffee",
              "amount": 6.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        // User A δημιουργεί expense
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                );

        // Βρίσκουμε το expense που ανήκει στον User A
        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                PageRequest.of(0, 10)
                        )
                        .getContent()
                        .get(0)
                        .getId();

        // User B προσπαθεί να δει το expense του User A
        mockMvc.perform(
                        get("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + otherToken
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void userShouldNotDeleteAnotherUsersExpense() throws Exception {

        AppUser otherUser = appUserRepository.save(
                new AppUser(
                        null,
                        "delete-other@example.com",
                        "fake-hash"
                )
        );

        String otherToken =
                jwtService.generateToken(
                        otherUser.getEmail()
                );

        String json = """
            {
              "description": "Do not delete me",
              "amount": 18.00,
              "category": "OTHER",
              "date": "2026-09-20"
            }
            """;

        // User A δημιουργεί το expense
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                );

        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                PageRequest.of(0, 10)
                        )
                        .getContent()
                        .get(0)
                        .getId();

        // User B προσπαθεί να το διαγράψει
        mockMvc.perform(
                        delete("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + otherToken
                                )
                )
                .andExpect(
                        status().isNotFound()
                );

        // Επιβεβαιώνουμε ότι το expense εξακολουθεί να υπάρχει
        boolean stillExists =
                expenseRepository
                        .findByIdAndUser(
                                expenseId,
                                user
                        )
                        .isPresent();

        assertTrue(stillExists);
    }

    @Test
    void ownerShouldDeleteOwnExpense() throws Exception {

        String json = """
            {
              "description": "Delete my expense",
              "amount": 22.00,
              "category": "OTHER",
              "date": "2026-09-20"
            }
            """;

        // Ο User A δημιουργεί το expense
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                );

        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                PageRequest.of(0, 10)
                        )
                        .getContent()
                        .get(0)
                        .getId();

        // Ο ίδιος owner το διαγράφει
        mockMvc.perform(
                        delete("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        boolean stillExists =
                expenseRepository
                        .findByIdAndUser(
                                expenseId,
                                user
                        )
                        .isPresent();

        assertFalse(stillExists);
    }

    @Test
    void userShouldNotUpdateAnotherUsersExpense() throws Exception {

        AppUser otherUser = appUserRepository.save(
                new AppUser(
                        null,
                        "update-other@example.com",
                        "fake-hash"
                )
        );

        String otherToken =
                jwtService.generateToken(
                        otherUser.getEmail()
                );

        String createJson = """
            {
              "description": "Original expense",
              "amount": 10.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                )
                .andExpect(
                        status().isCreated()
                );

        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                PageRequest.of(0, 10)
                        )
                        .getContent()
                        .get(0)
                        .getId();

        String updateJson = """
            {
              "description": "Hacked expense",
              "amount": 99.99,
              "category": "OTHER",
              "date": "2026-09-20"
            }
            """;

        // User B προσπαθεί να αλλάξει expense του User A
        mockMvc.perform(
                        put("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + otherToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                )
                .andExpect(
                        status().isNotFound()
                );

        Expense expenseAfterAttempt =
                expenseRepository
                        .findByIdAndUser(
                                expenseId,
                                user
                        )
                        .orElseThrow();

        assertEquals(
                "Original expense",
                expenseAfterAttempt.getDescription()
        );

        assertEquals(
                new BigDecimal("10.00"),
                expenseAfterAttempt.getAmount()
        );
    }

    @Test
    void ownerShouldUpdateOwnExpense() throws Exception {

        String createJson = """
            {
              "description": "Old dinner",
              "amount": 12.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                )
                .andExpect(
                        status().isCreated()
                );

        Long expenseId =
                expenseRepository
                        .findByUser(
                                user,
                                PageRequest.of(0, 10)
                        )
                        .getContent()
                        .get(0)
                        .getId();

        String updateJson = """
            {
              "description": "Updated dinner",
              "amount": 25.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        mockMvc.perform(
                        put("/expenses/" + expenseId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Updated dinner")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(25.00)
                );

        Expense updatedExpense =
                expenseRepository
                        .findByIdAndUser(
                                expenseId,
                                user
                        )
                        .orElseThrow();

        assertEquals(
                "Updated dinner",
                updatedExpense.getDescription()
        );

        assertEquals(
                new BigDecimal("25.00"),
                updatedExpense.getAmount()
        );
    }

    @Test
    void statsShouldIncludeOnlyLoggedInUsersExpenses() throws Exception {

        AppUser otherUser = appUserRepository.save(
                new AppUser(
                        null,
                        "stats-other@example.com",
                        "fake-hash"
                )
        );

        String otherToken =
                jwtService.generateToken(
                        otherUser.getEmail()
                );

        String expenseA1 = """
            {
              "description": "Coffee A",
              "amount": 5.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        String expenseA2 = """
            {
              "description": "Fuel A",
              "amount": 30.00,
              "category": "TRANSPORT",
              "date": "2026-09-20"
            }
            """;

        String expenseB = """
            {
              "description": "Dinner B",
              "amount": 100.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        // User A δημιουργεί 5 + 30 = 35
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseA1)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseA2)
                )
                .andExpect(status().isCreated());

        // User B δημιουργεί 100
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + otherToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseB)
                )
                .andExpect(status().isCreated());

        // User A ζητά τα δικά του total stats
        mockMvc.perform(
                        get("/expenses/stats/total")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.totalAmount")
                                .value(35.00)
                );
    }

    @Test
    void statsByCategoryShouldIncludeOnlyLoggedInUsersExpenses() throws Exception {

        AppUser otherUser = appUserRepository.save(
                new AppUser(
                        null,
                        "category-other@example.com",
                        "fake-hash"
                )
        );

        String otherToken =
                jwtService.generateToken(
                        otherUser.getEmail()
                );

        String expenseA1 = """
            {
              "description": "Coffee A",
              "amount": 5.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        String expenseA2 = """
            {
              "description": "Lunch A",
              "amount": 15.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        String expenseA3 = """
            {
              "description": "Fuel A",
              "amount": 30.00,
              "category": "TRANSPORT",
              "date": "2026-09-20"
            }
            """;

        String expenseB = """
            {
              "description": "Dinner B",
              "amount": 100.00,
              "category": "FOOD",
              "date": "2026-09-20"
            }
            """;

        // User A
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseA1)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseA2)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseA3)
                )
                .andExpect(status().isCreated());

        // User B
        mockMvc.perform(
                        post("/expenses")
                                .header(
                                        "Authorization",
                                        "Bearer " + otherToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseB)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/expenses/stats/by-category")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[?(@.category == 'FOOD')].totalAmount")
                                .value(20.00)
                )
                .andExpect(
                        jsonPath("$[?(@.category == 'TRANSPORT')].totalAmount")
                                .value(30.00)
                );
    }
}