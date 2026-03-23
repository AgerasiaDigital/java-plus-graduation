package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.event.ParticipationRequestDto;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.debug("Get requests for user with id = {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} not found", userId);
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.debug("Add participation request: userId = {}, eventId = {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event is not published");
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        Request savedRequest = requestRepository.save(request);
        log.info("Request created: {}", savedRequest);

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Cancel request: userId = {}, requestId = {}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id = %d not found", requestId)));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("User is not request owner");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        log.info("Request cancelled: {}", request);

        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.debug("Get participants: userId = {}, eventId = {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.debug("Change request status: userId = {}, eventId = {}, update = {}", userId, eventId, updateRequest);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }

        List<Request> requests = requestRepository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        if (requests.isEmpty()) {
            throw new NotFoundException("Requests not found");
        }

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        int availableSlots = event.getParticipantLimit() - confirmedCount.intValue();

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        RequestStatus newStatus = RequestStatus.from(updateRequest.getStatus().toString())
                .orElseThrow(() -> new ValidationException("Invalid status"));

        if (newStatus.equals(RequestStatus.CONFIRMED)) {
            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                for (Request request : requests) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toDto(request));
                }
            } else {
                for (Request request : requests) {
                    if (availableSlots > 0) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confirmed.add(RequestMapper.toDto(request));
                        availableSlots--;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        rejected.add(RequestMapper.toDto(request));
                    }
                }

                if (availableSlots == 0) {
                    List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                    for (Request pending : pendingRequests) {
                        if (!updateRequest.getRequestIds().contains(pending.getId())) {
                            pending.setStatus(RequestStatus.REJECTED);
                            requestRepository.save(pending);
                        }
                    }
                }
            }
        } else if (newStatus.equals(RequestStatus.REJECTED)) {
            for (Request request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }
}