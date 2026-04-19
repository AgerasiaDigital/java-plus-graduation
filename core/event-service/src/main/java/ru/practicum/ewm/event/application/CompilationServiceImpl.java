package ru.practicum.ewm.event.application;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.client.event.dto.CompilationDto;
import ru.practicum.ewm.api.client.event.dto.EventShortDto;
import ru.practicum.ewm.api.client.event.dto.NewCompilationDto;
import ru.practicum.ewm.api.client.event.dto.UpdateCompilationRequestDto;
import ru.practicum.ewm.api.error.EntityAlreadyExistsException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.event.domain.Compilation;
import ru.practicum.ewm.event.domain.CompilationRepository;
import ru.practicum.ewm.event.domain.Event;
import ru.practicum.ewm.event.domain.EventRepository;
import ru.practicum.ewm.event.infrastructure.mapper.CompilationMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.debug("Fetching compilations with pinned={}, from={}, size={}", pinned, from, size);
        List<Compilation> compilations = (pinned != null)
                ? compilationRepository.findAll(Optional.of(pinned), from, size)
                : compilationRepository.findAll(Optional.empty(), from, size);
        List<CompilationDto> result = compilations.stream()
                .map(compilationMapper::toDto)
                .map(this::addConfirmedRequestsAndViews)
                .collect(Collectors.toList());
        log.debug("Found {} compilations", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.debug("Fetching compilation with id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation", "Id", compId));
        CompilationDto result = compilationMapper.toDto(compilation);
        log.debug("Found compilation: {}", result);
        return addConfirmedRequestsAndViews(result);
    }

    @Override
    public CompilationDto saveCompilation(NewCompilationDto request) {
        log.info("Creating new compilation: {}", request);
        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new EntityAlreadyExistsException("Compilation", "title", request.getTitle());
        }

        Compilation compilation = compilationMapper.toCompilation(request);
        Set<Event> events = (request.getEvents() != null && !request.getEvents().isEmpty())
                ? loadEvents(request.getEvents())
                : new HashSet<>();
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto result = compilationMapper.toDto(savedCompilation);
        log.info("Compilation created successfully: {}", result);
        return addConfirmedRequestsAndViews(result);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto request) {
        log.info("Updating compilation id={} with data: {}", compId, request);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation", "Id", compId));

        if (request.getTitle() != null && !request.getTitle().isBlank() &&
                compilationRepository.existsByTitle(request.getTitle()) &&
                !compilation.getTitle().equalsIgnoreCase(request.getTitle())) {
            throw new EntityAlreadyExistsException("Compilation", "title", request.getTitle());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            compilation.getEvents().clear();
            Set<Event> events = (request.getEvents().isEmpty())
                    ? new HashSet<>()
                    : loadEvents(request.getEvents());
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        CompilationDto result = compilationMapper.toDto(updatedCompilation);
        log.info("Compilation updated successfully: {}", result);
        return addConfirmedRequestsAndViews(result);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id={}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new EntityNotFoundException("Compilation", "Id", compId);
        }
        compilationRepository.deleteById(compId);
        log.info("Compilation with id={} deleted successfully", compId);
    }

    private Set<Event> loadEvents(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdIn(new HashSet<>(eventIds));
        if (events.size() != eventIds.size()) {
            throw new EntityNotFoundException("Some events not found for IDs: " + eventIds);
        }
        return new HashSet<>(events);
    }

    private CompilationDto addConfirmedRequestsAndViews(CompilationDto compilationDto) {
        if (compilationDto.getEvents() != null) {
            for (EventShortDto eventDto : compilationDto.getEvents()) {
                eventDto.setConfirmedRequests(0L);
                eventDto.setRating(0.0);
            }
        }
        return compilationDto;
    }
}