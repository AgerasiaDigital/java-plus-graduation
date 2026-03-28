package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.dto.ApiError;
import ru.practicum.ewm.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "Not found",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.warn("Unexpected error: {}", e.getMessage());
        return new ApiError(
                e.getMessage(),
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }
}
