package ru.practicum.ewm.event.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.domain.Compilation;

@Repository
public interface JpaCompilationRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph(attributePaths = {"events", "events.category"})
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category"})
    Page<Compilation> findAll(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
        "FROM Compilation c WHERE LOWER(TRIM(c.title)) = LOWER(TRIM(:title))")
    boolean existsByTitleIgnoreCaseAndTrim(@Param("title") String title);
}