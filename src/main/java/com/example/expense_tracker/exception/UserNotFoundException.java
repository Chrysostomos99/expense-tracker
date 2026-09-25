package com.example.expense_tracker.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("Authenticated user not found");
    }
}