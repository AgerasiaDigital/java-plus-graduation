package ru.practicum.request.client;

import org.springframework.stereotype.Component;
import ru.practicum.request.dto.EventInfoDto;
import ru.practicum.request.exception.NotFoundException;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public EventInfoDto getEventInfo(Long eventId) {
        throw new NotFoundException(String.format("Event with id = %d not found (event-service unavailable)", eventId));
    }
}
