package ru.practicum.compilation.client;

import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.EventShortDto;

import java.util.List;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public List<EventShortDto> getByIds(List<Long> ids) {
        return List.of();
    }
}
