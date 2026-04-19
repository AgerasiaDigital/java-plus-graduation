package ru.practicum.ewm.request.presentation.error;

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
import ru.practicum.ewm.api.error.EntityAlreadyExistsException;
import ru.practicum.ewm.api.error.EntityNotFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * Handles exceptions when a required entity (Request, User, Event) is not found.
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
     * Handles exceptions when a user tries to create a participation request for an event
     * for which they have already submitted a request.
     * <br>
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(EntityAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        log.warn("Entity already exists: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("Integrity constraint has been violated.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles violations of business logic within the request service.
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

        String message = "A participation request for this event by this user might already exist.";

        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("Integrity constraint has been violated.")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}