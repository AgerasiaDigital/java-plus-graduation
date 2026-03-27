package ru.practicum.events.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.events.dto.EventRequestRulesDto;
import ru.practicum.ewm.model.event.Event;

@RestController
@RequestMapping("/internal")
public class EventsInternalController {
    private final EventRepository eventRepository;

    public EventsInternalController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/events/{eventId}/request-rules")
    public EventRequestRulesDto getEventRequestRules(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        EventRequestRulesDto dto = new EventRequestRulesDto();
        dto.setState(event.getState());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setInitiatorId(event.getInitiator().getId());
        return dto;
    }

    @GetMapping("/events/exists-by-category/{categoryId}")
    public boolean existsByCategory(@PathVariable Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }
}

