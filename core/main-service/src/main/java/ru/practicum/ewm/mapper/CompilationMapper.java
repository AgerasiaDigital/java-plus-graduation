package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.event.Event;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static Compilation toNewCompilation(NewCompilationDto dto, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setEvents(events);
        compilation.setPinned(dto.isPinned());
        compilation.setTitle(dto.getTitle());
        return compilation;
    }

    public static CompilationDto toDto(Compilation compilation, Set<EventShortDto> eventShortDtos) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setEvents(eventShortDtos);
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());
        return dto;
    }

    public static void updateFields(Compilation compilation, UpdateCompilationRequest request, Set<Event> events) {
        if (request.hasEvents()) {
            compilation.setEvents(events);
        }
        if (request.hasPinned()) {
            compilation.setPinned(request.getPinned());
        }
        if (request.hasTitle()) {
            compilation.setTitle(request.getTitle());
        }
    }
}