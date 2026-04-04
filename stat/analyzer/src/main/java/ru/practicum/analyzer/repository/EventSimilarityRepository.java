package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA = :eventId OR s.eventB = :eventId ORDER BY s.score DESC")
    List<EventSimilarity> findByEvent(Long eventId);
}
