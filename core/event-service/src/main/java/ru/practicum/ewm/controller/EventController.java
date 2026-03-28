package ru.practicum.ewm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.EventInitiatorIdFilter;
import ru.practicum.ewm.service.EventService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
public class EventController {
    private final EventService eventService;
    private final StatClient statClient;

    private void saveHit(HttpServletRequest request) {
        try {
            statClient.hit(new EndpointHitDto(
                    "ewm-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.warn("Failed to save hit: {}", e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto, HttpServletRequest request) throws IOException {
        log.info("Request to create event, userId={}", userId);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId,
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
        return eventService.updateEventByCreator(null, eventId, updateEventRequest);
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
        log.info("Public request for detailed information on the event with id: {}", eventId);
        saveHit(request);
        return eventService.getEvent(eventId);
    }
}
