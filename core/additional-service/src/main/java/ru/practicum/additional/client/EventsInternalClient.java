package ru.practicum.additional.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "events-service")
public interface EventsInternalClient {

    @GetMapping("/internal/events/exists-by-category/{categoryId}")
    boolean existsByCategory(@PathVariable("categoryId") Long categoryId);
}

