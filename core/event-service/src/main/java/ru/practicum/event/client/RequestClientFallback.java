package ru.practicum.event.client;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RequestClientFallback implements RequestClient {
    @Override
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        return Map.of();
    }

    @Override
    public boolean hasConfirmedRequest(Long userId, Long eventId) {
        return false;
    }
}
