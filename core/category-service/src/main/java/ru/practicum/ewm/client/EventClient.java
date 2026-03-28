package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service", fallback = EventClient.EventClientFallback.class)
public interface EventClient {

    @GetMapping("/internal/events/exists-by-category")
    boolean existsByCategory(@RequestParam("categoryId") Long categoryId);

    @Component
    @Slf4j
    class EventClientFallback implements EventClient {
        @Override
        public boolean existsByCategory(Long categoryId) {
            log.warn("event-service unavailable, assuming no events for category {}", categoryId);
            return false;
        }
    }
}
