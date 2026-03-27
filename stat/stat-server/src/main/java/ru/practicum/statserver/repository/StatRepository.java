package ru.practicum.statserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.statserver.model.StatModel;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatRepository extends JpaRepository<StatModel, Long> {

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(
                s.app,
                s.uri,
               CASE WHEN :unique = TRUE
                    THEN COUNT(DISTINCT s.ip)
                    ELSE COUNT(s.ip)
               END
            )
            FROM StatModel s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY 3 DESC
            """)
    List<ViewStatsDto> getStatWithoutUris(LocalDateTime start, LocalDateTime end, Boolean unique);

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(
                s.app,
                s.uri,
               CASE WHEN :unique = TRUE
                    THEN COUNT(DISTINCT s.ip)
                    ELSE COUNT(s.ip)
               END
            )
            FROM StatModel s
            WHERE s.timestamp BETWEEN :start AND :end
              AND (:uris IS NULL OR s.uri IN :uris)
            GROUP BY s.app, s.uri
            ORDER BY 3 DESC
            """)
    List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end,
                               Collection<String> uris, Boolean unique);
}