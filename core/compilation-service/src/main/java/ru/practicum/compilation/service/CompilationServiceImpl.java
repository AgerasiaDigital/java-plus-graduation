package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.client.EventClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.EventShortDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.exception.NotFoundException;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    public CompilationDto create(NewCompilationDto request) {
        Compilation compilation = new Compilation();
        compilation.setPinned(request.isPinned());
        compilation.setTitle(request.getTitle());
        if (request.getEvents() != null) {
            compilation.setEventIds(request.getEvents());
        }
        Compilation saved = compilationRepository.save(compilation);
        return toDto(saved);
    }

    @Override
    public void deleteById(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id = %d not found", compId));
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));
        if (request.hasEvents()) {
            compilation.setEventIds(request.getEvents() != null ? request.getEvents() : new HashSet<>());
        }
        if (request.hasPinned()) {
            compilation.setPinned(request.getPinned());
        }
        if (request.hasTitle()) {
            compilation.setTitle(request.getTitle());
        }
        compilationRepository.save(compilation);
        return toDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Compilation> compilations = pinned != null
                ? compilationRepository.findByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable).getContent();
        return compilations.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));
        return toDto(compilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());

        Set<EventShortDto> events = new HashSet<>();
        if (compilation.getEventIds() != null && !compilation.getEventIds().isEmpty()) {
            try {
                List<EventShortDto> fetched = eventClient.getByIds(List.copyOf(compilation.getEventIds()));
                events.addAll(fetched);
            } catch (Exception e) {
                log.warn("Failed to fetch events for compilation {}: {}", compilation.getId(), e.getMessage());
            }
        }
        dto.setEvents(events);
        return dto;
    }
}
