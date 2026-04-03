package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.EventSimilarityId;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Query("SELECT es FROM EventSimilarity es WHERE es.id.eventA = :eventId OR es.id.eventB = :eventId")
    List<EventSimilarity> findByEvent(long eventId);

    @Query("SELECT es FROM EventSimilarity es WHERE es.id.eventA IN :eventIds OR es.id.eventB IN :eventIds")
    List<EventSimilarity> findByEventIn(List<Long> eventIds);
}
