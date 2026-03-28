package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.event.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiator(Long initiator, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator IN :initiatorIds")
    Page<Event> findByInitiatorIn(@Param("initiatorIds") List<Long> initiatorIds, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = :state")
    Page<Event> findByState(@Param("state") String state, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.categoryId IN :categoryIds")
    Page<Event> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
