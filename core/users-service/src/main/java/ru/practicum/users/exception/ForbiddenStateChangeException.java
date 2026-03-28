package ru.practicum.users.exception;

public class ForbiddenStateChangeException extends RuntimeException {
    public ForbiddenStateChangeException(String message) {
        super(message);
    }
}