package ru.practicum.ewm.api.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.api.client.event.dto.EventInternalDto;

@FeignClient(name = "event-service", path = "/internal/events", configuration = EventClientConfiguration.class)
public interface EventClient {

    /**
     * Fetches event details by its ID.
     * @param eventId The ID of the event to retrieve.
     * @return The EventInternalDto containing the event details.
     */
    @GetMapping("/{eventId}")
    EventInternalDto getEventById(@PathVariable("eventId") Long eventId);
}