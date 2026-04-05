package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.UserEventInteraction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteraction, UserEventInteraction.Pk> {

    Optional<UserEventInteraction> findByUserIdAndEventId(long userId, long eventId);

    List<UserEventInteraction> findByUserIdOrderByLastInteractionAtDesc(long userId, Pageable pageable);

    boolean existsByUserIdAndEventId(long userId, long eventId);

    @Query("SELECT COALESCE(SUM(u.maxWeight), 0.0) FROM UserEventInteraction u WHERE u.eventId = :eventId")
    double sumMaxWeightsForEvent(@Param("eventId") long eventId);

    @Query("SELECT u.eventId, COALESCE(SUM(u.maxWeight), 0.0) FROM UserEventInteraction u WHERE u.eventId IN :ids GROUP BY u.eventId")
    List<Object[]> sumMaxWeightsForEvents(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT u.eventId FROM UserEventInteraction u WHERE u.userId = :uid")
    List<Long> findEventIdsByUserId(@Param("uid") long userId);
}
