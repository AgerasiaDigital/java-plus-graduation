package ru.practicum.ewm.dto.compilation;

import lombok.Data;
import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.Set;

@Data
public class CompilationDto {
    private Long id;
    private Set<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
