package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.CollectorGrpcClient;
import ru.practicum.event.client.RequestClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.exception.ValidationException;
import ru.practicum.event.filter.EventAdminFilter;
import ru.practicum.event.filter.EventInitiatorIdFilter;
import ru.practicum.event.filter.EventPublicFilter;
import ru.practicum.event.service.EventService;
import ru.practicum.grpc.stats.collector.ActionTypeProto;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final CollectorGrpcClient collectorGrpcClient;
    private final RequestClient requestClient;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        log.info("POST /users/{}/events", userId);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId,
                                                     EventInitiatorIdFilter filter,
                                                     PageRequestDto pageRequestDto) {
        filter.setUserId(userId);
        log.info("GET /users/{}/events", userId);
        return eventService.getEventByUserId(filter, pageRequestDto.toPageable());
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventFullDescription(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getEventFullDescription(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreator(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.updateEventByCreator(userId, eventId, updateEventRequest);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("PATCH /admin/events/{}", eventId);
        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsAdmin(EventAdminFilter filter, PageRequestDto pageRequestDto) {
        log.info("GET /admin/events");
        return eventService.adminSearchEvents(filter, pageRequestDto);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@Valid EventPublicFilter filter,
                                         PageRequestDto pageRequestDto) {
        log.info("GET /events");
        return eventService.publicSearchEvents(filter, pageRequestDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                 @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("GET /events/{} userId={}", eventId, userId);
        return eventService.getEvent(eventId, userId);
    }

    @PutMapping("/events/{eventId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeEvent(@PathVariable Long eventId,
                          @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("PUT /events/{}/like userId={}", eventId, userId);
        if (!requestClient.isUserParticipant(userId, eventId)) {
            throw new ValidationException("User " + userId + " has not participated in event " + eventId);
        }
        collectorGrpcClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    @GetMapping("/events/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
                                                  @RequestParam(defaultValue = "10") int maxResults) {
        log.info("GET /events/recommendations userId={}", userId);
        return eventService.getRecommendations(userId, maxResults);
    }
}
