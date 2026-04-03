package ru.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.request.dto.EventInfoDto;

@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {
    @GetMapping("/internal/events/{eventId}")
    EventInfoDto getEventInfo(@PathVariable Long eventId);
}
