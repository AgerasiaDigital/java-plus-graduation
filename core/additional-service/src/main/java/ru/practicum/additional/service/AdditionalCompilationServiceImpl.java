package ru.practicum.additional.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.additional.client.RequestsInternalClient;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationParam;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.service.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Primary
@Transactional
public class AdditionalCompilationServiceImpl implements CompilationService {
    private static final Logger log = LoggerFactory.getLogger(AdditionalCompilationServiceImpl.class);

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestsInternalClient requestsInternalClient;

    public AdditionalCompilationServiceImpl(CompilationRepository compilationRepository,
                                             EventRepository eventRepository,
                                             EventMapper eventMapper,
                                             RequestsInternalClient requestsInternalClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.requestsInternalClient = requestsInternalClient;
    }

    @Override
    public CompilationDto create(NewCompilationDto request) {
        log.debug("Create compilation request: {}", request);

        Set<Event> events = new HashSet<>();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
        }

        Compilation compilation = compilationRepository.save(CompilationMapper.toNewCompilation(request, events));
        log.info("Compilation created: {}", compilation);

        Set<EventShortDto> eventShortDtos = buildEventShortDtos(events);
        return CompilationMapper.toDto(compilation, eventShortDtos);
    }

    @Override
    public void deleteById(Long compId) {
        log.debug("Delete compilation with id = {}", compId);

        if (!compilationRepository.existsById(compId)) {
            log.warn("Compilation with id = {} not found", compId);
            throw new NotFoundException(String.format("Compilation with id = %d not found", compId));
        }

        compilationRepository.deleteById(compId);
        log.info("Compilation with id = {} deleted", compId);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        log.debug("Update compilation with id = {}: {}", compId, request);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        Set<Event> events = compilation.getEvents();
        if (request.hasEvents()) {
            if (request.getEvents() != null && !request.getEvents().isEmpty()) {
                events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            } else {
                events = new HashSet<>();
            }
        }

        CompilationMapper.updateFields(compilation, request, events);
        compilationRepository.save(compilation);
        log.info("Compilation updated: {}", compilation);

        Set<EventShortDto> eventShortDtos = buildEventShortDtos(events);
        return CompilationMapper.toDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(CompilationParam param) {
        log.debug("Get compilations: {}", param);

        int page = param.from() / param.size();
        Pageable pageable = PageRequest.of(page, param.size());

        List<Compilation> compilations;
        if (param.pinned() != null) {
            compilations = compilationRepository.findByPinned(param.pinned(), pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> eventShortDtos = buildEventShortDtos(compilation.getEvents());
                    return CompilationMapper.toDto(compilation, eventShortDtos);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        log.debug("Get compilation with id = {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        Set<EventShortDto> eventShortDtos = buildEventShortDtos(compilation.getEvents());
        return CompilationMapper.toDto(compilation, eventShortDtos);
    }

    private Set<EventShortDto> buildEventShortDtos(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return new HashSet<>();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequestsMap = requestsInternalClient.countConfirmedRequestsByEventIds(eventIds);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                    Long views = 0L;
                    return eventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(java.util.stream.Collectors.toSet());
    }
}

