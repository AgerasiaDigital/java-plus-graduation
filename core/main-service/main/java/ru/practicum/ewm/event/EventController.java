package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.service.RequestService;

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
    private final RequestService requestService;

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
                               @Valid @RequestBody NewEventDto newEventDto, HttpServletRequest request) throws IOException {
        log.info("Request to create event, userId={}", userId);
        log.debug("newEventDto: {}", newEventDto);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId,
                                                     EventInitiatorIdFilter eventInitiatorIdFilter,
                                                     PageRequestDto pageRequestDto) {
        log.info("User event request, userId={}", userId);
        Collection<EventShortDto> events = eventService.getEventByUserId(eventInitiatorIdFilter,
                pageRequestDto.toPageable());
        log.info("Found events: {}", events.size());
        events.forEach(ev -> log.debug("EVENT: {}", ev));
        return events;
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventFullDescription(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("User requested the event with detailed description, userId={}, eventId={}", userId, eventId);
        EventFullDto eventFullDto = eventService.getEventFullDescription(userId, eventId);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreator(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Event edit request by user, userId={}, eventId={}", userId, eventId);
        log.debug("{}", updateEventRequest);
        EventFullDto eventFullDto = eventService.updateEventByCreator(userId, eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> checkUserEventParticipation(@PathVariable Long userId,
                                                                     @PathVariable Long eventId) {
        log.info("User event participation request, userId={}, eventId={}", userId, eventId);
        List<ParticipationRequestDto> participationRequestDto = requestService.getEventParticipants(userId, eventId);
        log.debug("EVENTS: {}", participationRequestDto);
        return participationRequestDto;
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequest(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Request to change the status of event participation eventId={}, user userId={}", eventId, userId);
        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = requestService.changeRequestStatus(userId,
                eventId,
                eventRequestStatusUpdateRequest);
        log.debug("EVENTS: {}", eventRequestStatusUpdateResult);
        return eventRequestStatusUpdateResult;
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Request to edit the event by the admin, eventId={}", eventId);
        log.debug("{}", updateEventRequest);

        EventFullDto eventFullDto = eventService.updateEventByAdmin(eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
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
        log.debug("Request parameters: {}", eventPublicFilter);
        log.info("client ip: {}", request.getRemoteAddr());
        saveHit(request);
        return eventService.publicSearchEvents(eventPublicFilter, pageRequestDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                 HttpServletRequest request) {
        log.info("Public request for detailed information on the event with id: {}", eventId);
        log.info("client ip: {}", request.getRemoteAddr());
        saveHit(request);
        return eventService.getEvent(eventId);
    }
}