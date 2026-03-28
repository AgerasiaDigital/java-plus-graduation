package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.filter.EventAdminFilter;
import ru.practicum.event.filter.EventInitiatorIdFilter;
import ru.practicum.event.filter.EventPublicFilter;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final StatClient statClient;

    private void saveHit(HttpServletRequest request) {
        try {
            statClient.hit(new EndpointHitDto(
                    "ewm-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()));
        } catch (Exception e) {
            log.warn("Failed to save hit: {}", e.getMessage());
        }
    }

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
                                         PageRequestDto pageRequestDto,
                                         HttpServletRequest request) {
        log.info("GET /events");
        saveHit(request);
        return eventService.publicSearchEvents(filter, pageRequestDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("GET /events/{}", eventId);
        saveHit(request);
        return eventService.getEvent(eventId);
    }
}
