package ru.practicum.ewm.api.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * An abstract base class for global exception handlers in microservices.
 * Provides standardized, reusable handlers for shared or generic exceptions.
 */
@Slf4j
public abstract class BaseExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> String.format("Field '%s': %s. Rejected value: '%s'",
                error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
            .collect(Collectors.toList());
        String errorMessage = "Validation error(s): " + String.join("; ", errors);
        log.warn(errorMessage);
        return ApiError.builder()
            .errors(errors)
            .status(HttpStatus.BAD_REQUEST)
            .reason("Incorrectly made request due to validation errors.")
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
            .map(violation -> String.format("Parameter '%s': value '%s' %s",
                extractParameterName(violation),
                violation.getInvalidValue(),
                violation.getMessage()))
            .collect(Collectors.toList());
        String errorMessage = "Validation constraint(s) violated: " + String.join("; ", errors);
        log.warn(errorMessage);
        return ApiError.builder()
            .errors(errors)
            .status(HttpStatus.BAD_REQUEST)
            .reason("One or more validation constraints were violated.")
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameter(final MissingServletRequestParameterException e) {
        String errorMessage = "Required request parameter is not present: " + e.getParameterName();
        log.warn(errorMessage);
        return ApiError.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("Incorrectly made request.")
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String message = String.format("Parameter '%s' should be of type '%s' but was '%s'.",
            e.getName(), e.getRequiredType().getSimpleName(), e.getValue());
        log.warn("Type mismatch for parameter '{}': {}", e.getName(), e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("Incorrectly made request due to a type mismatch for a request parameter.")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("Malformed request body: {}", e.getMessage());
        return ApiError.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("Malformed JSON request.")
            .message("The request body is malformed or unreadable: " + e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("An unexpected error occurred: {}", e.getMessage(), e);
        return ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .reason("An unexpected error occurred on the server.")
            .message("An internal server error has occurred: " + e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    private String extractParameterName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        if (propertyPath.contains(".")) {
            return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
        }
        return propertyPath;
    }
}