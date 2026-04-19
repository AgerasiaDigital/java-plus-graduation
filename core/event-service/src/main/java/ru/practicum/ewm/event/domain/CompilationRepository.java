package ru.practicum.ewm.event.domain;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository {

    Compilation save(Compilation compilation);

    void deleteById(Long compilationId);

    Optional<Compilation> findById(Long compilationId);

    boolean existsById(Long compilationId);

    /**
     * Finds all compilations, optionally filtered by their "pinned" status.
     *
     * @param pinned An Optional boolean. If present, filters compilations by their pinned status.
     *               If empty, all compilations are returned.
     * @param from   The starting index.
     * @param size   The number of compilations to return.
     * @return A list of compilations.
     */
    List<Compilation> findAll(Optional<Boolean> pinned, int from, int size);

    /**
     * Checks if a compilation with the given title already exists.
     * The check should be case-insensitive and trim whitespace.
     *
     * @param title The title to check.
     * @return true if a compilation with that title exists, false otherwise.
     */
    boolean existsByTitle(String title);
}