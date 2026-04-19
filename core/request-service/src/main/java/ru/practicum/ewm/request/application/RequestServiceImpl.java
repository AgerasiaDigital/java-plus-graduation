package ru.practicum.ewm.request.application;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.client.event.EventClient;
import ru.practicum.ewm.api.client.event.dto.EventInternalDto;
import ru.practicum.ewm.api.client.event.enums.EventState;
import ru.practicum.ewm.api.client.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.ewm.api.client.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.api.client.user.UserClient;
import ru.practicum.ewm.api.error.BusinessRuleViolationException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.request.application.params.EventRequestStatusUpdateRequestParams;
import ru.practicum.ewm.request.domain.ParticipationRequest;
import ru.practicum.ewm.request.domain.RequestRepository;
import ru.practicum.ewm.request.domain.RequestStatus;
import ru.practicum.ewm.request.infrastructure.mapper.RequestMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long requestEventId) {
        ParticipationRequest result = checkRequest(userId, requestEventId);
        requestRepository.save(result);
        return requestMapper.toRequestDto(result);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest result = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with Id = " + userId + " and Request", "Id", userId));
        result.setStatus(RequestStatus.CANCELED);
        requestRepository.save(result);
        return requestMapper.toRequestDto(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId) {
        userClient.checkUserExists(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated).reversed())
                .map(requestMapper::toRequestDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        EventInternalDto event = eventClient.getEventById(eventId);
        if (!event.getInitiatorId().equals(userId)) {
            throw new EntityNotFoundException("Event with Id = " + eventId + " when initiator",
                "Id", userId);
        }
        return requestRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated).reversed())
                .map(requestMapper::toRequestDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateRequestsStatus(
        EventRequestStatusUpdateRequestParams requestParams) {
        Long userId = requestParams.getUserId();
        Long eventId = requestParams.getEventId();
        List<Long> requestIdsForUpdate = requestParams.getRequestIds();
        RequestStatus statusUpdate = requestParams.getStatus();
        if (!statusUpdate.equals(RequestStatus.REJECTED) && !statusUpdate.equals(RequestStatus.CONFIRMED)) {
            throw new BusinessRuleViolationException("Only REJECTED and CONFIRMED statuses are allowed");
        }
        EventInternalDto event = eventClient.getEventById(eventId);
        if (!event.getInitiatorId().equals(userId)) {
            throw new EntityNotFoundException("Event with Id = " + eventId + " when initiator", "Id", userId);
        }
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new BusinessRuleViolationException("Event moderation or participant limit is not set");
        }
        if (!requestRepository.doAllRequestsBelongToEvent(requestIdsForUpdate, eventId)) {
            throw new BusinessRuleViolationException("Not all requests are for event with Id = " + eventId);
        }
        if (requestRepository
              .countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new BusinessRuleViolationException("Event participant limit reached");
        }
        LinkedHashMap<Long, ParticipationRequest> requestsMap = requestRepository.findAllByIdIn(requestIdsForUpdate).stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .collect(Collectors.toMap(
                        ParticipationRequest::getId, Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        requestsMap.values().forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new BusinessRuleViolationException("Cannot update request with status " + request.getStatus() +
                        ". Only requests with PENDING status can be updated.");
            }
        });
        EventRequestStatusUpdateResultDto result = new EventRequestStatusUpdateResultDto();
        if (statusUpdate == RequestStatus.REJECTED) {
            requestsMap.values().forEach(request -> {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(requestMapper.toRequestDto(request));
            });
            requestRepository.saveAll(requestsMap.values());
            return result;
        }

        final int[] availableRequests = {event.getParticipantLimit() -
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)};
        requestsMap.values().forEach(request -> {
             if (availableRequests[0] > 0) {
                 request.setStatus(RequestStatus.CONFIRMED);
                 result.getConfirmedRequests().add(requestMapper.toRequestDto(request));
                 availableRequests[0]--;
             } else {
                 request.setStatus(RequestStatus.REJECTED);
                 result.getRejectedRequests().add(requestMapper.toRequestDto(request));
             }
        });
        requestRepository.saveAll(requestsMap.values());

        if (availableRequests[0] == 0) {
            log.debug("Participant limit for event {} reached. Rejecting all other pending requests.", eventId);
            requestRepository.rejectAllPendingRequestsForEvent(eventId);
        }

        return result;
    }

    @Override
    @Transactional
    public Map<Long, Long> getConfirmedRequestCounts(Set<Long> eventIds) {

        if (eventIds == null || eventIds.isEmpty()) {
            log.warn("Nothing to fetch");
            return Map.of();
        }

        Map<Long, Long> confirmedRequestsCounts = requestRepository.getConfirmedRequestCounts(eventIds);
        log.info("Fetched confirmed request counts: {}", confirmedRequestsCounts);
        return requestRepository.getConfirmedRequestCounts(eventIds);
    }

    private ParticipationRequest checkRequest(Long userId, Long requestEventId) {
        userClient.checkUserExists(userId);
        EventInternalDto event = eventClient.getEventById(requestEventId);
        if (requestRepository.existsByEventIdAndRequesterId(requestEventId, userId)) {
            throw new BusinessRuleViolationException("User has already requested for this event");
        }
        if (event.getInitiatorId().equals(userId)) {
            throw new BusinessRuleViolationException("User cannot participate in his own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new BusinessRuleViolationException("Event must be published");
        }
        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(requestEventId, RequestStatus.CONFIRMED) >=
                        event.getParticipantLimit()) {
            throw new BusinessRuleViolationException("Event participant limit reached");
        }
        ParticipationRequest newRequest = new ParticipationRequest();
        newRequest.setRequesterId(userId);
        newRequest.setEventId(requestEventId);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }
        return newRequest;
    }

    public void checkUserParticipation(long userId, long eventId) {
        if (!requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED)) {
            throw new EntityNotFoundException("User " + userId + " has not participated in event " + eventId);
        }
    }

}
