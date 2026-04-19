package ru.practicum.ewm.event.presentation.error;

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
import ru.practicum.ewm.api.error.EntityDeletedException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.api.error.IllegalLikeException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * Handles exceptions when a required entity (Event, Category, Compilation, User) is not found.
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
     * Handles exceptions when trying to create an entity (Category, Compilation) with a name/title that already exists.
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
     * Handles the specific business rule violation of a user trying to like an event
     * they have not participated in.
     * <br>
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalLikeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalLikeException(IllegalLikeException e) {
        log.warn("Illegal like attempt: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("The conditions for liking the event are not met.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles generic violations of business logic within the event service.
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
     * Handles the specific business rule where a category cannot be deleted if it is associated with events.
     * <br>
     * Maps to HTTP 409 Conflict, which is more appropriate than 404 for this case.
     */
    @ExceptionHandler(EntityDeletedException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Corrected from NOT_FOUND to CONFLICT
    public ApiError handleEntityDeletedException(EntityDeletedException e) {
        log.warn("Cannot delete entity because it is in use: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("The resource cannot be deleted due to existing associations.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Handles database integrity violations with context-specific messaging.
     * <br>
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.warn("Database integrity violation: {}", e.getMessage(), e);

        String rootCauseMessage = e.getMostSpecificCause().getMessage().toLowerCase();
        String userMessage;

        if (rootCauseMessage.contains("categories_name_key")) {
            userMessage = "A category with this name already exists.";
        } else if (rootCauseMessage.contains("compilations_title_key")) {
            userMessage = "A compilation with this title already exists.";
        } else {
            userMessage = "A database integrity constraint was violated.";
        }

        return ApiError.builder()
            .status(HttpStatus.CONFLICT)
            .reason("Integrity constraint has been violated.")
            .message(userMessage)
            .timestamp(LocalDateTime.now())
            .build();
    }
}