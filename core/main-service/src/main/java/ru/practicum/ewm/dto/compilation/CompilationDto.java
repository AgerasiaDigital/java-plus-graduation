package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.Set;

@Data
public class CompilationDto {
    private Set<EventShortDto> events; // unique

    @NotNull
    private Long id;

    @NotNull
    private Boolean pinned;

    @NotNull
    private String title;
}
