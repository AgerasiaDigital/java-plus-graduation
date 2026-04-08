package ru.practicum.ewm.request.infrastructure.persistence;

import java.util.Comparator;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.request.domain.ParticipationRequest;
import ru.practicum.ewm.request.domain.RequestRepository;
import ru.practicum.ewm.request.domain.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestRepositoryAdapter implements RequestRepository {

    private final JpaRequestRepository jpaRequestRepository;
    private final Sort defaultSort = Sort.by(Sort.Direction.DESC, "created");

    @Override
    public void save(ParticipationRequest request) {
        jpaRequestRepository.save(request);
    }

    @Override
    public void saveAll(Iterable<ParticipationRequest> requests) {
        jpaRequestRepository.saveAll(requests);
    }

    @Override
    public Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId) {
        return jpaRequestRepository.findByIdAndRequesterId(requestId, requesterId);
    }

    @Override
    public List<ParticipationRequest> findByRequesterId(Long requesterId) {
        return jpaRequestRepository.findByRequesterId(requesterId).stream()
            .sorted(defaultSort.getOrderFor("created").isDescending() ?
                Comparator.comparing(ParticipationRequest::getCreated).reversed() :
                Comparator.comparing(ParticipationRequest::getCreated))
            .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequest> findByEventId(Long eventId) {
        return jpaRequestRepository.findByEventId(eventId).stream()
            .sorted(defaultSort.getOrderFor("created").isDescending() ?
                Comparator.comparing(ParticipationRequest::getCreated).reversed() :
                Comparator.comparing(ParticipationRequest::getCreated))
            .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequest> findAllByIdIn(List<Long> requestIds) {
        return jpaRequestRepository.findAllByIdIn(requestIds);
    }

    @Override
    public boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId) {
        return jpaRequestRepository.existsByEventIdAndRequesterId(eventId, requesterId);
    }

    @Override
    public boolean existsByRequesterIdAndEventIdAndStatus(Long eventId, Long requesterId, RequestStatus status) {
        return jpaRequestRepository.existsByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
    }

    @Override
    public int countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return jpaRequestRepository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public boolean doAllRequestsBelongToEvent(List<Long> requestIds, Long eventId) {
        return jpaRequestRepository.countByIdInAndEventId(requestIds, eventId) == requestIds.size();
    }

    @Override
    public void rejectAllPendingRequestsForEvent(Long eventId) {
        jpaRequestRepository.rejectAllPendingForEvent(eventId);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestCounts(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> actualCounts = jpaRequestRepository.countConfirmedRequestsForEventIds(eventIds)
            .stream()
            .collect(Collectors.toMap(
                JpaRequestRepository.ConfirmedRequestCountProjection::getEventId,
                JpaRequestRepository.ConfirmedRequestCountProjection::getRequestCount
            ));

        Map<Long, Long> result = eventIds.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                id -> 0L
            ));

        result.putAll(actualCounts);

        return result;
    }
}