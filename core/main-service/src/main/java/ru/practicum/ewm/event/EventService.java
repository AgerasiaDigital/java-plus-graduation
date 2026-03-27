package ru.practicum.ewm.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.*;

import java.util.Collection;
import java.util.List;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);

    Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                               Pageable pageable);

    EventFullDto getEventFullDescription(Long userId, Long eventId);

    EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, PageRequestDto pageRequestDto);

    List<EventShortDto> publicSearchEvents(EventPublicFilter eventPublicFilter, PageRequestDto pageRequestDto);

    List<ParticipationRequestDto> checkUserEventParticipation(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatusRequest(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    EventFullDto getEvent(Long eventId);
}
