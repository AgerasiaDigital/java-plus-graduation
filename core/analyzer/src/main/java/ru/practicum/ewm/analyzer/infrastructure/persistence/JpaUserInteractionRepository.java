package ru.practicum.ewm.analyzer.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.domain.Recommendation;
import ru.practicum.ewm.analyzer.domain.UserInteraction;
import ru.practicum.ewm.analyzer.domain.UserInteractionRepository;

public interface JpaUserInteractionRepository extends UserInteractionRepository, JpaRepository<UserInteraction, Long> {

    @Override
    @Query("""
        SELECT DISTINCT ui.eventId
        FROM UserInteraction ui
        WHERE ui.userId = :userId
        """)
    List<Long> findInteractedEvents(@Param("userId") long userId);

    @Query("""
        SELECT new ru.practicum.ewm.analyzer.domain.Recommendation(T.eventId, SUM(T.maxWeight))
        FROM (
            SELECT ui.eventId AS eventId, MAX(ui.weight) AS maxWeight
            FROM UserInteraction ui
            WHERE ui.eventId IN :eventIds
            GROUP BY ui.userId, ui.eventId
            ) AS T
            GROUP BY T.eventId
        """)
    List<Recommendation> findInteractionsCount(@Param("eventIds") Collection<Long> eventIds);

    @Override
    default List<Long> findRecentlyInteractedEvents(long userId, int maxResults) {
        return findRecentlyInteractedEventsPageable(userId, PageRequest.of(0, maxResults));
    }

    @Query("""
        SELECT ui.eventId
        FROM UserInteraction ui
        WHERE ui.userId = :userId
        ORDER BY ui.interactionTime DESC
        """)
    List<Long> findRecentlyInteractedEventsPageable(@Param("userId") long userId, Pageable pageable);

    @Query("""
        SELECT ui.eventId AS eventId, MAX(ui.weight) AS weight
        FROM UserInteraction ui
        WHERE ui.userId = :userId AND ui.eventId IN :eventIds
        GROUP BY ui.eventId
    """)
    List<InteractionWeightProjection> findMaxWeightsForUserAndEvents(@Param("userId") long userId, @Param("eventIds") Collection<Long> eventIds);

    @Override
    default Map<Long, Double> findInteractionWeights(long userId, Collection<Long> eventIds) {
        return findMaxWeightsForUserAndEvents(userId, eventIds).stream()
            .collect(Collectors.toMap(
                InteractionWeightProjection::getEventId,
                InteractionWeightProjection::getWeight
            ));
    }

    interface InteractionWeightProjection {
        Long getEventId();
        Double getWeight();
    }
}