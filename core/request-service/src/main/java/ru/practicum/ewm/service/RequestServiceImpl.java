package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.event.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.debug("Get user requests, userId={}", userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.debug("Add participation request, userId={}, eventId={}", userId, eventId);

        // Check if request already exists
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        // Get event details
        EventFullDto event;
        try {
            event = eventClient.getEvent(eventId);
        } catch (Exception e) {
            throw new NotFoundException("Event not found");
        }

        // Check if event is published
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Event is not published");
        }

        // Check participant limit
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        Request request = new Request();
        request.setEventId(eventId);
        request.setRequesterId(userId);

        if (Boolean.FALSE.equals(event.getRequestModeration())) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        request = requestRepository.save(request);
        log.info("Request created: {}", request.getId());

        return RequestMapper.toDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Cancel request, userId={}, requestId={}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("User is not the requester");
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);

        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.debug("Get event participants, userId={}, eventId={}", userId, eventId);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        log.debug("Change request status, userId={}, eventId={}", userId, eventId);

        EventFullDto event;
        try {
            event = eventClient.getEvent(eventId);
        } catch (Exception e) {
            throw new NotFoundException("Event not found");
        }

        if (!event.getInitiator().equals(userId)) {
            throw new ConflictException("User is not the event initiator");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        int availableSlots = event.getParticipantLimit() - confirmedCount.intValue();

        for (Long requestId : request.getRequestIds()) {
            Request req = requestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Request not found"));

            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Request does not belong to this event");
            }

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request is not pending");
            }

            if ("CONFIRMED".equals(request.getStatus())) {
                if (availableSlots > 0) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    availableSlots--;
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toDto(req));
                    continue;
                }
            } else if ("REJECTED".equals(request.getStatus())) {
                req.setStatus(RequestStatus.REJECTED);
            }

            requestRepository.save(req);
            confirmed.add(RequestMapper.toDto(req));
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }
}
