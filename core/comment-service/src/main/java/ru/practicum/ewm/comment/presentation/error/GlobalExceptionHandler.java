package ru.practicum.ewm.comment.presentation.error;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.api.error.ApiError;
import ru.practicum.ewm.api.error.BaseExceptionHandler;
import ru.practicum.ewm.api.error.BusinessRuleViolationException;
import ru.practicum.ewm.api.error.EntityNotFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * Handles exceptions when a required entity (Comment, User, Event) is not found.
     * <br>
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.NOT_FOUND)
            .reason("The required object was not found.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles violations of business logic within the comment service.
     * <br>
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleBusinessRuleViolationException(BusinessRuleViolationException e) {
        log.warn("Business rule violation: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("For the requested operation the conditions are not met.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles database integrity violations.
     * <br>
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.warn("Database integrity violation: {}", e.getMessage(), e);

        String message = "A database integrity constraint was violated. " +
            "This may be due to an invalid reference to another entity (like an event).";

        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("Integrity constraint has been violated.")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles generic illegal argument exceptions as a fallback.
     * <br>
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage(), e);
        return ApiError.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("Incorrectly made request due to an invalid argument.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }
}