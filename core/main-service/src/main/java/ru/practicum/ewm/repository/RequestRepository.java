package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(Long eventId);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<Object[]> countConfirmedRequestsByEventIds(List<Long> eventIds);

    List<Request> findByIdInAndEventId(List<Long> ids, Long eventId);

    List<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<Request> findAllByIdInOrderByCreated(List<Long> requestIds);
}