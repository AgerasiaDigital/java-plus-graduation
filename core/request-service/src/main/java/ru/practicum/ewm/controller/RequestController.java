package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.event.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.debug("GET /users/{}/requests", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable @Positive Long userId,
                                                           @RequestParam @Positive Long eventId) {
        log.debug("POST /users/{}/requests?eventId={}", userId, eventId);
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.debug("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> checkUserEventParticipation(@PathVariable @Positive Long userId,
                                                                     @PathVariable @Positive Long eventId) {
        log.debug("GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequest(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        log.debug("PATCH /users/{}/events/{}/requests", userId, eventId);
        return requestService.changeRequestStatus(userId, eventId, request);
    }
}
