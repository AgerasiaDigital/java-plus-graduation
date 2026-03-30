package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.client.EventClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.EventShortDto;
import ru.practicum.compilation.model.Compilation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventClient eventClient;

    public CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());

        Set<EventShortDto> events = new HashSet<>();
        if (compilation.getEventIds() != null && !compilation.getEventIds().isEmpty()) {
            List<EventShortDto> fetched = eventClient.getByIds(List.copyOf(compilation.getEventIds()));
            events.addAll(fetched);
        }
        dto.setEvents(events);
        return dto;
    }
}
