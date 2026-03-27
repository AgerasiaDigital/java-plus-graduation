package ru.practicum.ewm.exception;

public class ForbiddenStateChangeException extends RuntimeException {
    public ForbiddenStateChangeException(String message) {
        super(message);
    }
}