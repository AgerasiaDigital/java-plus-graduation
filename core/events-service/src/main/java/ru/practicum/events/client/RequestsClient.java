package ru.practicum.events.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.event.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

/**
 * Feign client for inter-service interaction with {@code requests-service}.
 */
@FeignClient(name = "requests-service")
public interface RequestsClient {

    @PostMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> countConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getEventParticipants(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest
    );
}

