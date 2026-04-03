package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.analyzer.model.UserAction;
import ru.practicum.ewm.analyzer.model.UserActionId;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {

    List<UserAction> findByIdUserIdOrderByTimestampDesc(long userId, Pageable pageable);

    List<UserAction> findByIdUserId(long userId);

    @Query("SELECT ua FROM UserAction ua WHERE ua.id.eventId IN :eventIds")
    List<UserAction> findByEventIdIn(List<Long> eventIds);

    @Query("SELECT ua.id.eventId, SUM(ua.weight) FROM UserAction ua WHERE ua.id.eventId IN :eventIds GROUP BY ua.id.eventId")
    List<Object[]> sumWeightsByEventIds(List<Long> eventIds);
}
