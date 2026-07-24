package com.expensetracker.exception;

public class DBException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}