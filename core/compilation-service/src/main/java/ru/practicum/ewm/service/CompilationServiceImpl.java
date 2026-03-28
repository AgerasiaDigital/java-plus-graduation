package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.repository.CompilationRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    public CompilationDto create(NewCompilationDto request) {
        log.debug("Create compilation: {}", request);
        Compilation compilation = new Compilation();
        compilation.setPinned(request.isPinned());
        compilation.setTitle(request.getTitle());
        if (request.getEvents() != null) {
            compilation.setEventIds(new HashSet<>(request.getEvents()));
        }
        compilation = compilationRepository.save(compilation);
        log.info("Compilation created: {}", compilation.getId());
        return toDto(compilation);
    }

    @Override
    public void deleteById(Long compId) {
        log.debug("Delete compilation {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id=%d not found", compId));
        }
        compilationRepository.deleteById(compId);
        log.info("Compilation deleted: {}", compId);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        log.debug("Update compilation {}: {}", compId, request);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Compilation with id=%d not found", compId)));
        if (request.getEvents() != null) {
            compilation.setEventIds(new HashSet<>(request.getEvents()));
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        compilationRepository.save(compilation);
        log.info("Compilation updated: {}", compId);
        return toDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }
        return compilations.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Compilation with id=%d not found", compId)));
        return toDto(compilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());
        Set<Long> eventIds = compilation.getEventIds();
        if (eventIds != null && !eventIds.isEmpty()) {
            List<EventShortDto> events = eventClient.getEventsByIds(eventIds);
            dto.setEvents(new HashSet<>(events));
        } else {
            dto.setEvents(Collections.emptySet());
        }
        return dto;
    }
}
