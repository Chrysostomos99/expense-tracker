package com.example.expense_tracker.exception;

public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}