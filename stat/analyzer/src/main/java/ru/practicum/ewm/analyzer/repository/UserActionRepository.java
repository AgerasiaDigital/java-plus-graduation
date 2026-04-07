package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.analyzer.model.UserActionEntity;

import java.util.List;
import java.util.Optional;

public interface UserActionRepository extends JpaRepository<UserActionEntity, Long> {

    List<UserActionEntity> findByUserId(Long userId);

    List<UserActionEntity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Optional<UserActionEntity> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserActionEntity> findByEventIdIn(List<Long> eventIds);
}
