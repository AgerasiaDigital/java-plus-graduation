package ru.practicum.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Primary;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.EventAdminFilter;
import ru.practicum.ewm.event.EventInitiatorIdFilter;
import ru.practicum.ewm.event.EventPublicFilter;
import ru.practicum.ewm.event.EventSpecification;
import ru.practicum.ewm.event.StateTransitionValidator;
import ru.practicum.ewm.exception.AccessViolationException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.events.client.RequestsClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feign-based implementation of {@link EventService}.
 * Confirmed requests count is fetched from {@code requests-service}.
 */
@Service
@Primary
@Transactional
public class EventsServiceImpl implements EventService {
    private static final Logger log = LoggerFactory.getLogger(EventsServiceImpl.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final CategoryRepository categoryRepository;
    private final RequestsClient requestsClient;

    public EventsServiceImpl(EventRepository eventRepository,
                               UserRepository userRepository,
                               EventMapper eventMapper,
                               StatClient statClient,
                               CategoryRepository categoryRepository,
                               RequestsClient requestsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
        this.statClient = statClient;
        this.categoryRepository = categoryRepository;
        this.requestsClient = requestsClient;
    }

    private Map<Long, Long> getViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<String> uriList = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        StatsParamDto statsParamDto = new StatsParamDto();
        // Use a narrower interval around "now"
        statsParamDto.setStart(LocalDateTime.now().minusHours(1));
        statsParamDto.setEnd(LocalDateTime.now().plusHours(1));
        statsParamDto.setUris(uriList);
        statsParamDto.setIsUnique(true);

        try {
            List<ViewStatsDto> viewStatsDtoList = statClient.getStats(statsParamDto);
            return viewStatsDtoList.stream()
                    .collect(Collectors.toMap(
                            dto -> Long.parseLong(dto.getUri().substring(dto.getUri().lastIndexOf('/') + 1)),
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Error retrieving view statistics: {}", e.getMessage());
            return Map.of();
        }
    }

    private long getViewCount(Event event) {
        Map<Long, Long> map = getViews(List.of(event));
        return map.getOrDefault(event.getId(), 0L);
    }

    private Map<Long, Long> getRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        try {
            return requestsClient.countConfirmedRequestsByEventIds(eventIds);
        } catch (Exception e) {
            log.error("Error retrieving confirmed request counts: {}", e.getMessage());
            return Map.of();
        }
    }

    private long getRequestCount(Event event) {
        Map<Long, Long> map = getRequests(List.of(event));
        return map.getOrDefault(event.getId(), 0L);
    }

    private Map<Long, Boolean> checkAvailable(List<Event> events, Map<Long, Long> requestMap) {
        Map<Long, Boolean> availableMap = new HashMap<>();
        for (Event event : events) {
            if (event.getParticipantLimit() > 0) {
                Long confirmed = requestMap.getOrDefault(event.getId(), 0L);
                availableMap.put(event.getId(), confirmed < event.getParticipantLimit());
            } else {
                availableMap.put(event.getId(), true);
            }
        }
        return availableMap;
    }

    @Transactional
    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User id=%s not found", userId)));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("The event date must be at least 2 hours from now");
        }

        Event savedEvent = eventRepository.save(eventMapper.toEvent(newEventDto, user));
        return eventMapper.toFullDto(savedEvent, getRequestCount(savedEvent), getViewCount(savedEvent));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                                        Pageable pageable) {
        var spec = EventSpecification.withInitiatorId(eventInitiatorIdFilter);
        var page = eventRepository.findAll(spec, pageable);

        Map<Long, Long> viewsMap = getViews(page.getContent());
        Map<Long, Long> requestsMap = getRequests(page.getContent());

        return page.getContent().stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User id=%s not found", userId)));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format(
                    "Access denied! User userId=%s is not the creator of the event eventId=%s",
                    userId, eventId));
        }

        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event));
    }

    @Transactional
    @Override
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Events can only be edited when in Pending Moderation or Cancelled status");
        }

        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Editing events is allowed no later than 2 hours before they start.");
        }

        if (updateEventRequest.getEventDate() != null && updateEventRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Editing past events is prohibited");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User id=%s not found", userId)));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format(
                    "Access denied! User userId=%s is not the creator of the event eventId=%s",
                    userId, eventId));
        }

        EventState newState = event.getState();
        if (updateEventRequest.getStateAction() != null) {
            newState = StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), false);
        }

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }

        updateEventRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Events can only be edited when in Pending Moderation or Cancelled state");
        }

        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Editing events is allowed no later than 1 hour before they start");
        }

        if (updateEventRequest.getEventDate() != null && updateEventRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Editing past events is prohibited");
        }

        EventState state = event.getState();
        if (updateEventRequest.getStateAction() != null) {
            state = StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), true);
        }

        if (event.getState() == EventState.PENDING && state == EventState.PUBLISHED) {
            event.setPublishedOn(LocalDateTime.now());
        }

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }

        updateEventRequest.applyTo(event, category, newLocation, state);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();
        var spec = EventSpecification.withAdminFilter(eventAdminFilter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> requestsMap = getRequests(events);
        Map<Long, Long> viewsMap = getViews(events);

        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> publicSearchEvents(EventPublicFilter eventPublicFilter, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();

        EventSort sort = pageRequestDto.getSort();
        boolean sorByDate = sort == EventSort.EVENT_DATE;
        boolean sortByViews = sort == EventSort.VIEWS;
        boolean noSort = sort == null;

        var spec = EventSpecification.withPublicFilter(eventPublicFilter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> requestsMap = getRequests(events);
        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Boolean> availableMap = checkAvailable(events, requestsMap);

        if (Boolean.TRUE.equals(eventPublicFilter.getOnlyAvailable())) {
            events = events.stream()
                    .filter(e -> availableMap.getOrDefault(e.getId(), false))
                    .toList();
        }

        if (sortByViews) {
            events = events.stream()
                    .sorted(Comparator.comparingLong(e -> viewsMap.getOrDefault(e.getId(), 0L)))
                    .toList().reversed();
        }

        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> checkUserEventParticipation(Long userId, Long eventId) {
        return requestsClient.getEventParticipants(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult changeStatusRequest(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequest updateRequest) {
        return requestsClient.changeRequestStatus(userId, eventId, updateRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event));
    }
}

