package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.event.EventAdminFilter;
import ru.practicum.ewm.event.EventInitiatorIdFilter;
import ru.practicum.ewm.event.EventPublicFilter;
import ru.practicum.ewm.event.EventService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@Validated
public class EventsController {

    private static final Logger log = LoggerFactory.getLogger(EventsController.class);

    private final EventService eventService;
    private final StatClient statClient;

    public EventsController(EventService eventService, StatClient statClient) {
        this.eventService = eventService;
        this.statClient = statClient;
    }

    private void saveHit(HttpServletRequest request) {
        statClient.hit(new EndpointHitDto(
                "ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto,
                                 HttpServletRequest request) throws IOException {
        log.info("Request to create event, userId={}", userId);
        log.debug("newEventDto: {}", newEventDto);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable @Positive Long userId,
                                                       EventInitiatorIdFilter eventInitiatorIdFilter,
                                                       PageRequestDto pageRequestDto) {
        log.info("User event request, userId={}", userId);
        return eventService.getEventByUserId(eventInitiatorIdFilter, pageRequestDto.toPageable());
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventFullDescription(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("User requested the event with detailed description, userId={}, eventId={}", userId, eventId);
        return eventService.getEventFullDescription(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreator(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Event edit request by user, userId={}, eventId={}", userId, eventId);
        return eventService.updateEventByCreator(userId, eventId, updateEventRequest);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Request to edit the event by the admin, eventId={}", eventId);
        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsAdmin(EventAdminFilter eventAdminFilter,
                                              PageRequestDto pageRequestDto) {
        log.debug("Admin event request with parameters: {}", eventAdminFilter);
        return eventService.adminSearchEvents(eventAdminFilter, pageRequestDto);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@Valid EventPublicFilter eventPublicFilter,
                                           PageRequestDto pageRequestDto,
                                           HttpServletRequest request) {
        log.info("Public query of events with parameters: {}", eventPublicFilter);
        saveHit(request);
        return eventService.publicSearchEvents(eventPublicFilter, pageRequestDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                  HttpServletRequest request) {
        saveHit(request);
        return eventService.getEvent(eventId);
    }
}

