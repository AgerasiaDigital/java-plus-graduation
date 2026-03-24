package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationParam;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto request);

    void deleteById(Long compId);

    CompilationDto update(Long compId, UpdateCompilationRequest request);

    List<CompilationDto> getCompilations(CompilationParam param);

    CompilationDto getById(Long compId);
}