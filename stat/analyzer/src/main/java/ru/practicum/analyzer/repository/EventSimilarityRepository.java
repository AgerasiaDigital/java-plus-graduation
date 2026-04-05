package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarity.Pk> {

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA = :eid OR s.eventB = :eid")
    List<EventSimilarity> findAllInvolvingEvent(@Param("eid") long eventId);

    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);
}
