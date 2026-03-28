package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.event.EventAdminFilter;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventInitiatorIdFilter;
import ru.practicum.ewm.dto.event.EventPublicFilter;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.PageRequestDto;
import ru.practicum.ewm.dto.event.UpdateEventRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);

    Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter filter,
                                               org.springframework.data.domain.Pageable pageable);

    EventFullDto getEventFullDescription(Long userId, Long eventId);

    EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> adminSearchEvents(EventAdminFilter filter, PageRequestDto pageRequestDto);

    List<EventShortDto> publicSearchEvents(EventPublicFilter filter, PageRequestDto pageRequestDto);

    EventFullDto getEvent(Long eventId);

    boolean hasEventsWithCategory(Long categoryId);

    List<EventShortDto> getEventsByIds(Set<Long> ids);
}
