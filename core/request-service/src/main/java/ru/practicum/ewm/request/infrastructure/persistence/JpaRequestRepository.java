package ru.practicum.ewm.request.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.domain.ParticipationRequest;
import ru.practicum.ewm.request.domain.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface JpaRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    boolean existsByRequesterIdAndEventIdAndStatus(long requesterId, long eventId, RequestStatus status);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    int countByIdInAndEventId(List<Long> requestIds, Long eventId);

    @Modifying
    @Query("UPDATE ParticipationRequest r SET r.status = 'REJECTED' " +
        "WHERE r.eventId = :eventId AND r.status = 'PENDING'")
    void rejectAllPendingForEvent(@Param("eventId") Long eventId);

    List<ParticipationRequest> findByEventId(Long eventId);

    @Query("SELECT r.eventId as eventId, COUNT(r.id) as requestCount " +
        "FROM ParticipationRequest r " +
        "WHERE r.eventId IN :eventIds AND r.status = 'CONFIRMED' " +
        "GROUP BY r.eventId")
    List<ConfirmedRequestCountProjection> countConfirmedRequestsForEventIds(@Param("eventIds") Set<Long> eventIds);

    interface ConfirmedRequestCountProjection {
        Long getEventId();
        Long getRequestCount();
    }
}