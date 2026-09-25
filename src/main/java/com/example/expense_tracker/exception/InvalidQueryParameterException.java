package com.example.expense_tracker.exception;

public class InvalidQueryParameterException extends RuntimeException {

    public InvalidQueryParameterException(String message) {
        super(message);
    }
}