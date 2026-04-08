package ru.practicum.ewm.request.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository {

    void save(ParticipationRequest request);

    void saveAll(Iterable<ParticipationRequest> requests);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long eventId, Long requesterId, RequestStatus status);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean doAllRequestsBelongToEvent(List<Long> requestIds, Long eventId);

    /**
     * Efficiently rejects all PENDING requests for a given event.
     * This is useful when an event's participant limit is reached.
     */
    void rejectAllPendingRequestsForEvent(Long eventId);

    /**
     * Efficiently counts the number of confirmed requests for a set of event IDs.
     *
     * @param eventIds The set of event IDs to check.
     * @return A map where the key is the eventId and the value is the count of confirmed requests.
     */
    Map<Long, Long> getConfirmedRequestCounts(Set<Long> eventIds);
}