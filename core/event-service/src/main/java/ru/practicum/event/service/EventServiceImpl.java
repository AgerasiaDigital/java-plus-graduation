package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.grpc.AnalyzerGrpcClient;
import ru.practicum.event.client.RequestClient;
import ru.practicum.event.client.UserClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.exception.AccessViolationException;
import ru.practicum.event.exception.ConflictException;
import ru.practicum.event.exception.NotFoundException;
import ru.practicum.event.exception.ValidationException;
import ru.practicum.event.filter.EventAdminFilter;
import ru.practicum.event.filter.EventInitiatorIdFilter;
import ru.practicum.event.filter.EventPublicFilter;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.EventSpecification;
import ru.practicum.event.util.StateTransitionValidator;
import ru.practicum.ewm.stats.proto.analyzer.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final AnalyzerGrpcClient analyzerGrpcClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    private Map<Long, Double> getRatings(List<Event> events) {
        if (events == null || events.isEmpty()) return Map.of();
        List<Long> ids = events.stream().map(Event::getId).toList();
        try {
            return analyzerGrpcClient.getInteractionsCount(ids)
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            r -> (double) r.getScore()));
        } catch (Exception e) {
            log.error("Error retrieving ratings from analyzer: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<Long, Long> getConfirmedCounts(List<Event> events) {
        if (events == null || events.isEmpty()) return Map.of();
        List<Long> ids = events.stream().map(Event::getId).toList();
        return requestClient.getConfirmedCounts(ids);
    }

    private Map<Long, UserShortDto> getInitiators(List<Event> events) {
        if (events == null || events.isEmpty()) return Map.of();
        List<Long> userIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        try {
            return userClient.getByIds(userIds);
        } catch (Exception e) {
            log.error("Error retrieving user info: {}", e.getMessage());
            return userIds.stream().collect(Collectors.toMap(id -> id, id -> {
                UserShortDto dto = new UserShortDto();
                dto.setId(id);
                dto.setName("Unavailable");
                return dto;
            }));
        }
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("The event date must be at least 2 hours from now");
        }
        Event saved = eventRepository.save(eventMapper.toEvent(newEventDto, userId));
        UserShortDto initiator = userClient.getById(userId);
        return eventMapper.toFullDto(saved, 0L, 0.0, initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter filter, Pageable pageable) {
        Specification<Event> spec = EventSpecification.withInitiatorId(filter);
        Page<Event> events = eventRepository.findAll(spec, pageable);
        Map<Long, Double> ratingsMap = getRatings(events.getContent());
        Map<Long, Long> requestsMap = getConfirmedCounts(events.getContent());
        Map<Long, UserShortDto> usersMap = getInitiators(events.getContent());
        return events.getContent().stream()
                .map(event -> eventMapper.toShortDto(event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        ratingsMap.getOrDefault(event.getId(), 0.0),
                        usersMap.getOrDefault(event.getInitiatorId(), unknownUser(event.getInitiatorId()))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new AccessViolationException(String.format("User %s is not the initiator of event %s", userId, eventId));
        }
        UserShortDto initiator = userClient.getById(userId);
        Map<Long, Long> counts = getConfirmedCounts(List.of(event));
        Map<Long, Double> ratings = getRatings(List.of(event));
        return eventMapper.toFullDto(event, counts.getOrDefault(eventId, 0L),
                ratings.getOrDefault(eventId, 0.0), initiator);
    }

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
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new AccessViolationException(String.format("User %s is not the initiator of event %s", userId, eventId));
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
        Location newLocation = updateEventRequest.hasLocationDto()
                ? eventMapper.toLocation(updateEventRequest.getLocationDto()) : null;
        updateEventRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);
        UserShortDto initiator = userClient.getById(userId);
        Map<Long, Long> counts = getConfirmedCounts(List.of(event));
        Map<Long, Double> ratings = getRatings(List.of(event));
        return eventMapper.toFullDto(event, counts.getOrDefault(eventId, 0L),
                ratings.getOrDefault(eventId, 0.0), initiator);
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
        Location newLocation = updateEventRequest.hasLocationDto()
                ? eventMapper.toLocation(updateEventRequest.getLocationDto()) : null;
        updateEventRequest.applyTo(event, category, newLocation, state);
        eventRepository.save(event);
        UserShortDto initiator = userClient.getById(event.getInitiatorId());
        Map<Long, Long> counts = getConfirmedCounts(List.of(event));
        Map<Long, Double> ratings = getRatings(List.of(event));
        return eventMapper.toFullDto(event, counts.getOrDefault(eventId, 0L),
                ratings.getOrDefault(eventId, 0.0), initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearchEvents(EventAdminFilter filter, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();
        Specification<Event> spec = EventSpecification.withAdminFilter(filter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, Long> requestsMap = getConfirmedCounts(events);
        Map<Long, Double> ratingsMap = getRatings(events);
        Map<Long, UserShortDto> usersMap = getInitiators(events);
        return events.stream()
                .map(event -> eventMapper.toFullDto(event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        ratingsMap.getOrDefault(event.getId(), 0.0),
                        usersMap.getOrDefault(event.getInitiatorId(), unknownUser(event.getInitiatorId()))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> publicSearchEvents(EventPublicFilter filter, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();
        Specification<Event> spec = EventSpecification.withPublicFilter(filter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        if (events.isEmpty()) return List.of();
        Map<Long, Long> requestsMap = getConfirmedCounts(events);
        Map<Long, Double> ratingsMap = getRatings(events);
        Map<Long, UserShortDto> usersMap = getInitiators(events);
        if (filter.getOnlyAvailable()) {
            events = events.stream()
                    .filter(e -> {
                        int limit = e.getParticipantLimit();
                        return limit == 0 || requestsMap.getOrDefault(e.getId(), 0L) < limit;
                    }).toList();
        }
        if (pageRequestDto.getSort() == ru.practicum.event.dto.EventSort.VIEWS) {
            events = events.stream()
                    .sorted(Comparator.comparingDouble(e -> ratingsMap.getOrDefault(e.getId(), 0.0)))
                    .toList().reversed();
        }
        return events.stream()
                .map(event -> eventMapper.toShortDto(event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        ratingsMap.getOrDefault(event.getId(), 0.0),
                        usersMap.getOrDefault(event.getInitiatorId(), unknownUser(event.getInitiatorId()))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        UserShortDto initiator = userClient.getById(event.getInitiatorId());
        Map<Long, Long> counts = getConfirmedCounts(List.of(event));
        Map<Long, Double> ratings = getRatings(List.of(event));
        return eventMapper.toFullDto(event, counts.getOrDefault(eventId, 0L),
                ratings.getOrDefault(eventId, 0.0), initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public EventInfoDto getEventInfo(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        EventInfoDto dto = new EventInfoDto();
        dto.setId(event.getId());
        dto.setInitiatorId(event.getInitiatorId());
        dto.setState(event.getState().name());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllById(ids);
        Map<Long, Long> requestsMap = getConfirmedCounts(events);
        Map<Long, Double> ratingsMap = getRatings(events);
        Map<Long, UserShortDto> usersMap = getInitiators(events);
        return events.stream()
                .map(event -> eventMapper.toShortDto(event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        ratingsMap.getOrDefault(event.getId(), 0.0),
                        usersMap.getOrDefault(event.getInitiatorId(), unknownUser(event.getInitiatorId()))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        try {
            List<Long> recommendedIds = analyzerGrpcClient
                    .getRecommendationsForUser(userId, maxResults)
                    .map(RecommendedEventProto::getEventId)
                    .toList();
            return getEventsByIds(recommendedIds);
        } catch (Exception e) {
            log.error("Error getting recommendations for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void checkUserVisited(Long userId, Long eventId) {
        try {
            requestClient.checkUserParticipation(userId, eventId);
        } catch (Exception e) {
            throw new ValidationException(
                    String.format("User %d has not visited event %d", userId, eventId));
        }
    }

    private UserShortDto unknownUser(Long id) {
        UserShortDto dto = new UserShortDto();
        dto.setId(id);
        dto.setName("Unavailable");
        return dto;
    }
}
