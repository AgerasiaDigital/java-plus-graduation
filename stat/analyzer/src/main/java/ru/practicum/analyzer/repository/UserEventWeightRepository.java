package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.analyzer.model.UserEventWeight;

import java.util.List;
import java.util.Optional;

public interface UserEventWeightRepository extends JpaRepository<UserEventWeight, Long> {

    Optional<UserEventWeight> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserEventWeight> findByUserId(Long userId);

    @Query("SELECT w FROM UserEventWeight w WHERE w.userId = :userId ORDER BY w.lastInteractionTs DESC")
    List<UserEventWeight> findByUserIdOrderByTsDesc(Long userId);

    @Query("SELECT SUM(w.maxWeight) FROM UserEventWeight w WHERE w.eventId = :eventId")
    Double sumMaxWeightByEventId(Long eventId);
}
