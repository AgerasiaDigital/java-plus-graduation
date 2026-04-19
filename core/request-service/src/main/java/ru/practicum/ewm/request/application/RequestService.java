package ru.practicum.ewm.request.application;

import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.practicum.ewm.api.client.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.ewm.api.client.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.application.params.EventRequestStatusUpdateRequestParams;

public interface RequestService {

    ParticipationRequestDto createRequest(Long userId,Long requestEventId);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(@Positive Long userId, @Positive Long eventId);

    EventRequestStatusUpdateResultDto updateRequestsStatus(
        EventRequestStatusUpdateRequestParams requestParams);

    Map<Long, Long> getConfirmedRequestCounts(Set<Long> eventIds);

    void checkUserParticipation(long userId, long eventId);

}
