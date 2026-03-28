package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.client.CategoryClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.event.EventAdminFilter;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventInitiatorIdFilter;
import ru.practicum.ewm.dto.event.EventPublicFilter;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.PageRequestDto;
import ru.practicum.ewm.dto.event.UpdateEventRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final StatClient statClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        log.info("Request to create event, userId={}", userId);
        try {
            categoryClient.getCategoryById(newEventDto.getCategory());
        } catch (Exception e) {
            throw new NotFoundException("Category not found");
        }
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours in the future");
        }
        Event event = EventMapper.toNewEvent(newEventDto, userId);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        log.info("Event created: {}", event.getId());
        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter filter, Pageable pageable) {
        Page<Event> events = eventRepository.findByInitiator(filter.getUserId(), pageable);
        return events.getContent().stream()
                .map(e -> EventMapper.toShortDto(e, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getInitiator().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }
        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (userId != null && !event.getInitiator().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot update published event");
        }
        if (updateEventRequest.getStateAction() != null) {
            switch (updateEventRequest.getStateAction()) {
                case "CANCEL_REVIEW" -> event.setState(EventState.CANCELED);
                case "SEND_TO_REVIEW" -> event.setState(EventState.PENDING);
                case "PUBLISH_EVENT" -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Can only publish PENDING events");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case "REJECT_EVENT" -> {
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject published event");
                    }
                    event.setState(EventState.CANCELED);
                }
                default -> throw new ValidationException("Unknown stateAction: " + updateEventRequest.getStateAction());
            }
        }
        EventMapper.updateFields(event, updateEventRequest);
        event = eventRepository.save(event);
        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearchEvents(EventAdminFilter filter, PageRequestDto pageRequestDto) {
        int page = pageRequestDto.getFrom() / pageRequestDto.getSize();
        Pageable pageable = PageRequest.of(page, pageRequestDto.getSize());
        Page<Event> events = eventRepository.findAll(pageable);
        return events.getContent().stream()
                .map(e -> EventMapper.toFullDto(e, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> publicSearchEvents(EventPublicFilter filter, PageRequestDto pageRequestDto) {
        int page = pageRequestDto.getFrom() / pageRequestDto.getSize();
        Pageable pageable = PageRequest.of(page, pageRequestDto.getSize());
        Page<Event> events = eventRepository.findAll(pageable);
        return events.getContent().stream()
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .map(e -> EventMapper.toShortDto(e, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }
        long views = getViewCount(eventId);
        return EventMapper.toFullDto(event, categoryClient, userClient, 0, views);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEventsWithCategory(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByIds(Set<Long> ids) {
        List<Event> events = eventRepository.findByIdIn(new ArrayList<>(ids));
        return events.stream()
                .map(e -> EventMapper.toShortDto(e, categoryClient, userClient, 0, 0L))
                .toList();
    }

    private long getViewCount(Long eventId) {
        StatsParamDto params = new StatsParamDto();
        params.setStart(LocalDateTime.now().minusYears(10));
        params.setEnd(LocalDateTime.now());
        params.setUris(List.of("/events/" + eventId));
        params.setIsUnique(true);
        try {
            List<ViewStatsDto> stats = statClient.getStats(params);
            return stats.stream().mapToLong(ViewStatsDto::getHits).sum();
        } catch (Exception e) {
            log.warn("Failed to get view stats: {}", e.getMessage());
            return 0L;
        }
    }
}
