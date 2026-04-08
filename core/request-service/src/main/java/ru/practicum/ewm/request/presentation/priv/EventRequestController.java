package ru.practicum.ewm.request.presentation.priv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.ewm.api.client.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.ewm.api.client.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.application.params.EventRequestStatusUpdateRequestParams;
import ru.practicum.ewm.request.application.RequestService;
import ru.practicum.ewm.request.infrastructure.mapper.EnumMapper;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventRequestController {

    private final RequestService requestService;
    private final EnumMapper enumMapper;

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(
        @PathVariable @Positive Long userId,
        @PathVariable @Positive Long eventId) {
        log.info("Private: Received request to get list requests for event {} when initiator {}", eventId, userId);
        List<ParticipationRequestDto> result = requestService.getEventRequests(userId, eventId);
        log.info("Private: Received list requests for event {} when initiator {} : {}", eventId, userId, result);
        return result;
    }


    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResultDto updateRequestsStatus(
        @PathVariable @Positive Long userId,
        @PathVariable @Positive Long eventId,
        @Valid @RequestBody EventRequestStatusUpdateRequestDto requestStatusUpdate) {
        log.info("Private: Received request to change status requests {} for event {} when initiator {}",
            requestStatusUpdate.getRequestIds(), eventId, userId);
        EventRequestStatusUpdateRequestParams requestParams = EventRequestStatusUpdateRequestParams.builder()
            .userId(userId)
            .eventId(eventId)
            .requestIds(requestStatusUpdate.getRequestIds())
            .status(enumMapper.toModelRequestStatus(requestStatusUpdate.getStatus()))
            .build();
        EventRequestStatusUpdateResultDto result = requestService.updateRequestsStatus(requestParams);
        log.info("Private: Received list requests for event {} when initiator {} : {}", eventId, userId, result);
        return result;
    }

}