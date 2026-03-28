package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.CategoryClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.EventInitiatorIdFilter;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        log.info("Request to create event, userId={}", userId);

        // Validate category exists
        try {
            categoryClient.getCategoryById(newEventDto.getCategory());
        } catch (Exception e) {
            throw new NotFoundException("Category not found");
        }

        // Validate event date
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
        log.info("User event request, userId={}", filter.getUserId());

        Page<Event> events = eventRepository.findByInitiator(filter.getUserId(), pageable);

        return events.getContent().stream()
                .map(event -> EventMapper.toShortDto(event, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        log.info("User requested the event with detailed description, userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }

        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        log.info("Event edit request by user, userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ForbiddenException("Cannot update published event");
        }

        if (updateEventRequest.getStateAction() != null) {
            if (updateEventRequest.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            } else if (updateEventRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            }
        }

        EventMapper.updateFields(event, updateEventRequest);
        event = eventRepository.save(event);

        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearchEvents(EventAdminFilter filter, PageRequestDto pageRequestDto) {
        log.debug("Admin event request with parameters: {}", filter);

        int page = pageRequestDto.getFrom() / pageRequestDto.getSize();
        Pageable pageable = PageRequest.of(page, pageRequestDto.getSize());

        Page<Event> events = eventRepository.findAll(pageable);

        return events.getContent().stream()
                .map(event -> EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> publicSearchEvents(EventPublicFilter filter, PageRequestDto pageRequestDto) {
        log.info("Public query of events with parameters: {}", filter);

        int page = pageRequestDto.getFrom() / pageRequestDto.getSize();
        Pageable pageable = PageRequest.of(page, pageRequestDto.getSize());

        Page<Event> events = eventRepository.findAll(pageable);

        return events.getContent().stream()
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .map(event -> EventMapper.toShortDto(event, categoryClient, userClient, 0, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        log.info("Public request for detailed information on the event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }

        return EventMapper.toFullDto(event, categoryClient, userClient, 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEventsWithCategory(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }
}
