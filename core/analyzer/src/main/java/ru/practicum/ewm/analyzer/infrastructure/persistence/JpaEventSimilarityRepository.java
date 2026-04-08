package ru.practicum.ewm.analyzer.infrastructure.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.domain.EventSimilarity;
import ru.practicum.ewm.analyzer.domain.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.domain.Recommendation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface JpaEventSimilarityRepository extends EventSimilarityRepository, JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);

    @Override
    default List<Recommendation> findTopSimilarExcluding(long eventId, Collection<Long> excludedEvents, int maxResults) {
        Pageable limit = PageRequest.of(0, maxResults);
        return findTopSimilarExcludingPageable(eventId, excludedEvents, limit);
    }

    @Query(value = """
        SELECT new ru.practicum.ewm.analyzer.domain.Recommendation(
            CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END,
            es.score
        )
        FROM EventSimilarity es
        WHERE (es.eventA = :eventId OR es.eventB = :eventId)
          AND (CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END NOT IN :excludedEvents)
        ORDER BY es.score DESC
    """)
    List<Recommendation> findTopSimilarExcludingPageable(@Param("eventId") long eventId,
        @Param("excludedEvents") Collection<Long> excludedEvents,
        Pageable pageable);

    @Override
    default List<Long> findTopSimilarToSet(Collection<Long> recentlyInteractedEvents, int maxResults) {
        if (recentlyInteractedEvents == null || recentlyInteractedEvents.isEmpty()) {
            return List.of();
        }
        Pageable limit = PageRequest.of(0, maxResults);
        return findTopSimilarToSetPageable(recentlyInteractedEvents, limit);
    }

    @Query(value = """
        SELECT CASE WHEN es.eventA IN :eventSet THEN es.eventB ELSE es.eventA END as similarEventId
        FROM EventSimilarity es
        WHERE (es.eventA IN :eventSet AND es.eventB NOT IN :eventSet)
           OR (es.eventB IN :eventSet AND es.eventA NOT IN :eventSet)
        GROUP BY similarEventId
        ORDER BY AVG(es.score) DESC
    """)
    List<Long> findTopSimilarToSetPageable(@Param("eventSet") Collection<Long> recentlyInteractedEvents,
        Pageable pageable);

    @Override
    default Map<Long, List<Recommendation>> findNeighbourEventsFrom(Collection<Long> primaryEvents,
        Collection<Long> candidates,
        int maxNeighbours) {
        if (primaryEvents == null || primaryEvents.isEmpty() || candidates == null || candidates.isEmpty()) {
            return Map.of();
        }

        List<NeighborProjection> results = findTopNNeighborsNative(primaryEvents, candidates, maxNeighbours);

        return results.stream()
            .collect(Collectors.groupingBy(
                NeighborProjection::getPrimaryEvent,
                Collectors.mapping(
                    proj -> new Recommendation(proj.getNeighborEvent(), proj.getScore()),
                    Collectors.toList()
                )
            ));
    }

    interface NeighborProjection {
        Long getPrimaryEvent();
        Long getNeighborEvent();
        Double getScore();
    }

    @Query(value = """
        WITH ranked_neighbors AS (
            SELECT
                primary_event,
                neighbor_event,
                score,
                ROW_NUMBER() OVER(PARTITION BY primary_event ORDER BY score DESC) as rn
            FROM (
                SELECT event_a as primary_event, event_b as neighbor_event, score FROM event_similarities WHERE event_a IN :primaryEvents AND event_b IN :candidates
                UNION ALL
                SELECT event_b as primary_event, event_a as neighbor_event, score FROM event_similarities WHERE event_b IN :primaryEvents AND event_a IN :candidates
            ) as pairs
        )
        SELECT primary_event as primaryEvent, neighbor_event as neighborEvent, score
        FROM ranked_neighbors
        WHERE rn <= :maxNeighbours
        ORDER BY primary_event, score DESC
    """, nativeQuery = true)
    List<NeighborProjection> findTopNNeighborsNative(@Param("primaryEvents") Collection<Long> primaryEvents,
        @Param("candidates") Collection<Long> candidates,
        @Param("maxNeighbours") int maxNeighbours);
}