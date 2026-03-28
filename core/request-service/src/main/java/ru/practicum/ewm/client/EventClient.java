package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.event.EventFullDto;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/events/{eventId}")
    EventFullDto getEvent(@PathVariable("eventId") Long eventId);
}
