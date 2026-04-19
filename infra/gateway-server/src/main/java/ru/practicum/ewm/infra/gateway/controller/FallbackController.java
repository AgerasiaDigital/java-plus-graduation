package ru.practicum.ewm.infra.gateway.controller;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.ewm.api.error.ApiError;

@RestController
public class FallbackController {

    @RequestMapping("/service-fallback")
    public Mono<ResponseEntity<ApiError>> serviceFallback() {
        ApiError errorResponse = ApiError.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .reason("The service is temporarily unavailable.")
            .message("The requested service is not responding. Please try again later.")
            .timestamp(LocalDateTime.now())
            .build();

        ResponseEntity<ApiError> responseEntity =
            ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);

        return Mono.just(responseEntity);
    }
}