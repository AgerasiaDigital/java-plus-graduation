package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.service.EventService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {
    private final EventService eventService;

    @GetMapping("/by-ids")
    public List<EventShortDto> getEventsByIds(@RequestParam("ids") Set<Long> ids) {
        log.debug("GET /internal/events/by-ids: {}", ids);
        return eventService.getEventsByIds(ids);
    }

    @GetMapping("/exists-by-category")
    public boolean existsByCategory(@RequestParam("categoryId") Long categoryId) {
        log.debug("GET /internal/events/exists-by-category: {}", categoryId);
        return eventService.hasEventsWithCategory(categoryId);
    }
}
