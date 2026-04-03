package ru.practicum.event.client;

import org.springframework.stereotype.Component;
import ru.practicum.event.exception.ConflictException;

import java.util.List;
import java.util.Map;

@Component
public class RequestClientFallback implements RequestClient {
    @Override
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        return Map.of();
    }

    @Override
    public void checkUserParticipation(Long userId, Long eventId) {
        throw new ConflictException("Unable to verify participation: request-service unavailable");
    }
}
