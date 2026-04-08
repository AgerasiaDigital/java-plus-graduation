package ru.practicum.ewm.event.application;

import java.util.List;
import ru.practicum.ewm.api.client.event.dto.CompilationDto;
import ru.practicum.ewm.api.client.event.dto.NewCompilationDto;
import ru.practicum.ewm.api.client.event.dto.UpdateCompilationRequestDto;

public interface CompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);

    CompilationDto saveCompilation(NewCompilationDto request);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto request);

    void deleteCompilation(Long compId);
}