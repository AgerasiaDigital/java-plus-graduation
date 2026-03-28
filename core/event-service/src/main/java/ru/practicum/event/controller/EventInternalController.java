package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventInfoDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController {
    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventInfoDto getEventInfo(@PathVariable Long eventId) {
        log.debug("INTERNAL GET /internal/events/{}", eventId);
        return eventService.getEventInfo(eventId);
    }

    @GetMapping
    public List<EventShortDto> getEventsByIds(@RequestParam List<Long> ids) {
        log.debug("INTERNAL GET /internal/events?ids={}", ids);
        return eventService.getEventsByIds(ids);
    }
}
