package ru.practicum.ewm.api.error;

public class IllegalLikeException extends BusinessRuleViolationException {

    public IllegalLikeException(String message) {
        super(message);
    }
}
