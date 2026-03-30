package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.request.client.EventClient;
import ru.practicum.request.client.UserClient;
import ru.practicum.request.dto.EventInfoDto;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.exception.ConflictException;
import ru.practicum.request.exception.NotFoundException;
import ru.practicum.request.exception.ValidationException;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userClient.getById(userId); // проверяем существование пользователя
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        userClient.getById(userId); // бросит NotFoundException если нет

        EventInfoDto event = eventClient.getEventInfo(eventId); // бросит NotFoundException если нет

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Request already exists");
        }
        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Event is not published");
        }
        if (event.getParticipantLimit() > 0) {
            Long confirmed = requestRepository.countConfirmedByEventId(eventId);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEventId(eventId);
        request.setRequesterId(userId);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id = %d not found", requestId)));
        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("User is not request owner");
        }
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        EventInfoDto event = eventClient.getEventInfo(eventId);
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        EventInfoDto event = eventClient.getEventInfo(eventId);
        if (!event.getInitiatorId().equals(userId)) {
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
        Long confirmedCount = requestRepository.countConfirmedByEventId(eventId);
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
                    List<Request> pending = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                    List<Request> toReject = pending.stream()
                            .filter(p -> !updateRequest.getRequestIds().contains(p.getId()))
                            .toList();
                    toReject.forEach(p -> p.setStatus(RequestStatus.REJECTED));
                    requestRepository.saveAll(toReject);
                }
            }
        } else {
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

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        List<Object[]> raw = requestRepository.countConfirmedByEventIds(eventIds);
        return raw.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }
}
