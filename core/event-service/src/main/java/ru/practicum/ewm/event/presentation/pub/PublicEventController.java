package ru.practicum.ewm.event.presentation.pub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.event.dto.EventFullDto;
import ru.practicum.ewm.api.client.event.dto.EventShortDto;
import ru.practicum.ewm.event.application.EventService;
import ru.practicum.ewm.event.application.params.PublicEventSearchParams;
import ru.practicum.ewm.stats.client.aop.ActionType;
import ru.practicum.ewm.stats.client.aop.LogUserAction;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(
            @Valid PublicEventSearchParams params,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size,
            @RequestHeader(name = "X-Real-IP", required = false) String ipAddress) {

        log.info(
            "Public: Received request to get events with params: text={}, categories={}, paid={},"
                + " rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
            params.getText(), params.getCategories(), params.getPaid(), params.getRangeStart(),
            params.getRangeEnd(), params.getOnlyAvailable(), params.getSort(), from, size);

        List<EventShortDto> events = eventService.getEventsPublic(params, from, size);
        log.info("Public: Found {} events", events.size());
        return events;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    @LogUserAction(value = ActionType.VIEW)
    public EventFullDto getEventById(
            @PathVariable @Positive Long eventId,
            @RequestHeader(name = "X-Real-IP", required = false) String ipAddress) {
        log.info("Public: Received request to get event with id={}", eventId);
        EventFullDto event = eventService.getEventByIdPublic(eventId);
        log.info("Public: Found event: {}", event);
        return event;
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
        @RequestParam(name = "maxCount", defaultValue = "10") Integer maxCount) {
        return eventService.getRecommendations(userId, maxCount);
    }
}