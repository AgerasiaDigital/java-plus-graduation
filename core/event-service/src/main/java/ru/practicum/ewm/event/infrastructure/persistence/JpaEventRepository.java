package ru.practicum.ewm.event.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.domain.Event;
import ru.practicum.ewm.event.domain.EventState;

@Repository
public interface JpaEventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    boolean existsByCategoryId(Long categoryId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);
}