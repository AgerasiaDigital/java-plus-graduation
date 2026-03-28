package ru.practicum.users.exception;

public class AccessViolationException extends RuntimeException {
    public AccessViolationException(String message) {
        super(message);
    }
}