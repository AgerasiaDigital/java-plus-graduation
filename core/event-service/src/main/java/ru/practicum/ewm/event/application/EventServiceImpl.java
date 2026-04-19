package ru.practicum.ewm.event.application;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.client.event.dto.EventDtoWithRatingAndRequests;
import ru.practicum.ewm.api.client.event.dto.EventFullDto;
import ru.practicum.ewm.api.client.event.dto.EventInternalDto;
import ru.practicum.ewm.api.client.event.dto.EventShortDto;
import ru.practicum.ewm.api.client.event.dto.NewEventDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventUserRequestDto;
import ru.practicum.ewm.api.client.request.RequestClient;
import ru.practicum.ewm.api.client.user.UserClient;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.api.error.BusinessRuleViolationException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.api.error.IllegalLikeException;
import ru.practicum.ewm.api.utility.DtoMapper;
import ru.practicum.ewm.event.application.params.AdminEventSearchParams;
import ru.practicum.ewm.event.application.params.PublicEventSearchParams;
import ru.practicum.ewm.event.domain.Category;
import ru.practicum.ewm.event.domain.CategoryRepository;
import ru.practicum.ewm.event.domain.Event;
import ru.practicum.ewm.event.domain.EventRepository;
import ru.practicum.ewm.event.domain.EventState;
import ru.practicum.ewm.event.infrastructure.mapper.EventMapper;
import ru.practicum.ewm.event.infrastructure.mapper.LocationMapper;
import ru.practicum.ewm.stats.client.AnalyzerClient;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final RequestClient requestClient;
    private final AnalyzerClient analyzerClient;
    private final DtoMapper dtoMapper;

    private static final long MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN = 1;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsPublic(PublicEventSearchParams params, int from, int size) {
        log.info("Public search for events with params: {}, from={}, size={}", params, from, size);

        List<Event> foundEvents = eventRepository.findPublic(params, from, size);
        if (foundEvents.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> eventDtos = eventMapper.toEventShortDtoList(foundEvents);

        enrichEvents(eventDtos);

        if (Boolean.TRUE.equals(params.getOnlyAvailable())) {
            Map<Long, Integer> participantLimitMap = foundEvents.stream()
                .collect(Collectors.toMap(Event::getId, Event::getParticipantLimit));

            eventDtos = eventDtos.stream()
                .filter(dto -> {
                    Integer limit = participantLimitMap.get(dto.getId());
                    return limit == null || limit == 0 || dto.getConfirmedRequests() < limit;
                })
                .collect(Collectors.toList());
        }

        if (params.getSort() != null && params.getSort().equalsIgnoreCase("RATING")) {
            eventDtos.sort(Comparator.comparing(EventShortDto::getRating).reversed());
        }

        log.info("Public search prepared {} DTOs after enrichment and filtering.", eventDtos.size());
        return eventDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdPublic(Long eventId) {
        log.info("Public: Fetching event id={}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d not found or is not published.", eventId)));

        EventFullDto resultDto = eventMapper.toEventFullDto(event);

        enrichEvents(List.of(resultDto));

        log.info("Public: Found event id={} with title='{}', rating={}, confirmedRequests={}",
            eventId, resultDto.getTitle(), resultDto.getRating(), resultDto.getConfirmedRequests());

        return resultDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsAdmin(AdminEventSearchParams params, int from, int size) {

        log.debug(
            "Admin search for events with params: users={}, states={}, categories={}, "
                + "rangeStart={}, rangeEnd={}, from={}, size={}",
            params.getUsers(), params.getStates(), params.getCategories(), params.getRangeStart(),
            params.getRangeEnd(), from, size);

        List<Event> foundEvents = eventRepository.findAdmin(params, from, size);

        List<EventFullDto> result = eventMapper.toEventFullDtoList(foundEvents);

        log.debug("Admin search found {} events", result.size());
        return enrichEvents(result);
    }

    @Override
    public EventFullDto moderateEventByAdmin(Long eventId, UpdateEventAdminRequestDto requestDto) {
        log.info("Admin: Moderating event id={} with data: {}", eventId, requestDto);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " not found."));

        if (requestDto.getAnnotation() != null) {
            event.setAnnotation(requestDto.getAnnotation());
        }
        if (requestDto.getCategory() != null) {
            Category category = categoryRepository.findById(requestDto.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Category with id=" + requestDto.getCategory() + " not found for event update."));
            event.setCategory(category);
        }
        if (requestDto.getDescription() != null) {
            event.setDescription(requestDto.getDescription());
        }
        if (requestDto.getEventDate() != null) {
            event.setEventDate(requestDto.getEventDate());
        }
        if (requestDto.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(requestDto.getLocation()));
        }
        if (requestDto.getPaid() != null) {
            event.setPaid(requestDto.getPaid());
        }
        if (requestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(requestDto.getParticipantLimit());
        }
        if (requestDto.getRequestModeration() != null) {
            event.setRequestModeration(requestDto.getRequestModeration());
        }
        if (requestDto.getTitle() != null) {
            event.setTitle(requestDto.getTitle());
        }

        if (requestDto.getStateAction() != null) {
            switch (requestDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new BusinessRuleViolationException(
                                "Cannot publish the event because it's not in the PENDING state. Current state: " + event.getState());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN))) {
                        throw new BusinessRuleViolationException(
                                String.format("Cannot publish the event. Event date must be at least %d hour(s) in the future from the current moment. Event date: %s",
                                        MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN, event.getEventDate()));
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new BusinessRuleViolationException(
                                "Cannot reject the event because it has already been published. Current state: " + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    log.warn("Admin: Unknown state action for event update: {}", requestDto.getStateAction());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Admin: Event id={} moderated successfully. New state: {}", eventId, updatedEvent.getState());
        return enrichEvents(List.of(eventMapper.toEventFullDto(updatedEvent))).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByOwner(Long userId, int from, int size) {
        log.debug("Fetching events for owner (user) id: {}, from: {}, size: {}", userId, from, size);

        try {
            userClient.checkUserExists(userId);
        } catch (EntityNotFoundException e) {
            return Collections.emptyList(); // По спецификации API, если по заданным фильтрам не найдено ни одного события, возвращается пустой список
        }

        List<Event> foundEvents = eventRepository.findByInitiatorId(userId, from, size);

        if (foundEvents.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> result = eventMapper.toEventShortDtoList(foundEvents);
        log.debug("Found {} events for owner id: {}", result.size(), userId);
        return enrichEvents(result);
    }

    @Override
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequestDto requestDto) {
        log.info("User id={}: Updating event id={} with data: {}", userId, eventId, requestDto);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Event with id=%d and initiatorId=%d not found", eventId, userId)));

        if (!(event.getState() == EventState.PENDING || event.getState() == EventState.CANCELED)) {
            throw new BusinessRuleViolationException("Cannot update event: Only pending or canceled events can be changed. Current state: " + event.getState());
        }

        if (requestDto.getAnnotation() != null) {
            event.setAnnotation(requestDto.getAnnotation());
        }
        if (requestDto.getCategory() != null) {
            Category category = categoryRepository.findById(requestDto.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Category with id=" + requestDto.getCategory() + " not found."));
            event.setCategory(category);
        }
        if (requestDto.getDescription() != null) {
            event.setDescription(requestDto.getDescription());
        }
        if (requestDto.getEventDate() != null) {
            if (requestDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BusinessRuleViolationException("Event date must be at least two hours in the future from the current moment.");
            }
            event.setEventDate(requestDto.getEventDate());
        }
        if (requestDto.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(requestDto.getLocation()));
        }
        if (requestDto.getPaid() != null) {
            event.setPaid(requestDto.getPaid());
        }
        if (requestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(requestDto.getParticipantLimit());
        }
        if (requestDto.getRequestModeration() != null) {
            event.setRequestModeration(requestDto.getRequestModeration());
        }
        if (requestDto.getTitle() != null) {
            event.setTitle(requestDto.getTitle());
        }

        if (requestDto.getStateAction() != null) {
            switch (requestDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    log.warn("Unknown state action for user update: {}", requestDto.getStateAction());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("User id={}: Event id={} updated successfully.", userId, eventId);
        return enrichEvents(List.of(eventMapper.toEventFullDto(updatedEvent))).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventPrivate(Long userId, Long eventId) {
        log.debug("Fetching event id: {} for user id: {}", eventId, userId);

        userClient.checkUserExists(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Event with id=%d and initiatorId=%d not found", eventId, userId)));

        EventFullDto result = eventMapper.toEventFullDto(event);
        log.debug("Found event: {}", result);
        return enrichEvents(List.of(result)).getFirst();
    }

    @Override
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {
        log.info("Добавление события {} пользователем {}", newEventDto, userId);

        UserDto initiator = userClient.getUserById(userId);

        Long categoryId = newEventDto.getCategory();
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Категория " +
                "с id = " + categoryId + " не найдена"));

        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessRuleViolationException("Дата должна быть не ранее, чем через 2 часа от текущего момента");
        }

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiatorId(userId);
        EventFullDto savedEvent = eventMapper.toEventFullDto(eventRepository.save(event));
        savedEvent.setInitiator(dtoMapper.toUserShortDto(initiator));
        return enrichEvents(List.of(savedEvent)).getFirst();
    }

    @Override
    public void addLike(Long userId, Long eventId) {
        eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(
            () -> new EntityNotFoundException(
                "Event with id=" + eventId + " not found or is not published."));

        try {
            requestClient.checkUserParticipation(userId, eventId);
        } catch (EntityNotFoundException e) {
            throw new IllegalLikeException("User has not participated in this event and cannot like it.");
        }
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, Integer maxCount) {
        Map<Long, Double> recommendationsMap = analyzerClient.getRecommendationsForUser(userId, maxCount);
        if (recommendationsMap.isEmpty()) {
            log.warn("No recommendations found for user id={}", userId);
            return List.of();
        }

        List<Event> recommendedEvents = eventRepository.findAllByIdIn(recommendationsMap.keySet());
        log.info("Found {} recommended events for user id={}", recommendedEvents.size(), userId);
        return enrichEvents(eventMapper.toEventShortDtoList(recommendedEvents));
    }

    public EventInternalDto getEventById(Long eventId) {

        if (eventId == null ) {
            throw new EntityNotFoundException("Event", "Id", null);
        }

        return eventMapper.toEventInternalDto(eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event", "Id", eventId)));
    }

    /**
     * Enriches a list of event DTOs with the count of confirmed participation requests and their rating values.
     *
     * @param dtos A list of DTOs that extend a base type with getId(), setConfirmedRequests(), and setRating().
     * @param <T> The specific type of the DTO (e.g., EventFullDto, EventShortDto).
     * @return The same list of DTOs, now with the confirmedRequests and rating fields populated.
     */
    private <T extends EventDtoWithRatingAndRequests> List<T> enrichEvents(List<T> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> eventIds = dtos.stream()
            .map(T::getId)
            .collect(Collectors.toSet());

        Map<Long, Long> confirmedCountsMap = requestClient.getConfirmedRequestCounts(eventIds);
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(eventIds);

        dtos.forEach(dto -> {
            dto.setConfirmedRequests(confirmedCountsMap.getOrDefault(dto.getId(), 0L));
            dto.setRating(ratingsMap.getOrDefault(dto.getId(), 0.0));
        });

        return dtos;
    }
}