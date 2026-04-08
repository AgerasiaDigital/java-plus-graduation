package ru.practicum.ewm.event.application;

import java.util.List;
import ru.practicum.ewm.api.client.event.dto.EventFullDto;
import ru.practicum.ewm.api.client.event.dto.EventInternalDto;
import ru.practicum.ewm.api.client.event.dto.EventShortDto;
import ru.practicum.ewm.api.client.event.dto.NewEventDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventUserRequestDto;
import ru.practicum.ewm.event.application.params.AdminEventSearchParams;
import ru.practicum.ewm.event.application.params.PublicEventSearchParams;

public interface EventService {
    List<EventFullDto> getEventsAdmin(
        AdminEventSearchParams params,
        int from,
        int size
    );

    List<EventShortDto> getEventsByOwner(Long userId, int from, int size);

    EventFullDto getEventPrivate(Long userId, Long eventId);

    EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequestDto requestDto);

    EventFullDto moderateEventByAdmin(Long eventId, UpdateEventAdminRequestDto requestDto);

    List<EventShortDto> getEventsPublic(PublicEventSearchParams params, int from, int size);

    EventFullDto getEventByIdPublic(Long eventId);

    EventInternalDto getEventById(Long eventId);

    void addLike(Long userId, Long eventId);

    List<EventShortDto> getRecommendations(Long userId, Integer maxCount);
}