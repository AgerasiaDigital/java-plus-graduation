package ru.practicum.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.user.dto.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("400 {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ApiError(
                errors.entrySet().stream().map(en -> en.getKey() + ": " + en.getValue()).collect(Collectors.joining("; ")),
                "Request parameters was not valid",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("404 {}", e.getMessage());
        return new ApiError(e.getMessage(), "Required object was not found",
                HttpStatus.NOT_FOUND.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.warn("409 {}", e.getMessage());
        return new ApiError(e.getMessage(), "Parameter value conflict",
                HttpStatus.CONFLICT.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleJakartaConstraintViolationException(final jakarta.validation.ConstraintViolationException e) {
        log.warn("400 {}", e.getMessage());
        return new ApiError(e.getMessage(), "Request parameters was not valid",
                HttpStatus.BAD_REQUEST.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final org.hibernate.exception.ConstraintViolationException e) {
        log.warn("409 {}", e.getMessage());
        return new ApiError(e.getConstraintName() + ": " + e.getKind(),
                "Integrity constraint has been violated",
                HttpStatus.CONFLICT.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("400 {}", e.getMessage());
        return new ApiError(e.getMessage(), "Malformed JSON request",
                HttpStatus.BAD_REQUEST.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        log.warn("400 {}", e.getMessage());
        return new ApiError("Invalid value for parameter '" + e.getName() + "': " + e.getValue(),
                "Request parameters was not valid", HttpStatus.BAD_REQUEST.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParam(final MissingServletRequestParameterException e) {
        log.warn("400 {}", e.getMessage());
        return new ApiError("Missing required parameter: " + e.getParameterName(),
                "Missing required parameters", HttpStatus.BAD_REQUEST.name(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.warn("500 {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(), LocalDateTime.now().format(formatter));
    }
}
