package ru.practicum.compilation.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.EventShortDto;
import ru.practicum.compilation.model.Compilation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CompilationMapper {

    public CompilationDto toDto(Compilation compilation, Map<Long, EventShortDto> eventsById) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());

        Set<EventShortDto> events = compilation.getEventIds().stream()
                .filter(eventsById::containsKey)
                .map(eventsById::get)
                .collect(Collectors.toSet());
        dto.setEvents(events);
        return dto;
    }
}
