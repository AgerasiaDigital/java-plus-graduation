package ru.practicum.ewm.event.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import ru.practicum.ewm.event.application.params.AdminEventSearchParams;
import ru.practicum.ewm.event.application.params.PublicEventSearchParams;

public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(Long eventId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    /**
     * Finds all events created by a specific user, with pagination.
     *
     * @param initiatorId The ID of the user.
     * @param from        The starting index.
     * @param size        The number of events to return.
     * @return A list of events.
     */
    List<Event> findByInitiatorId(Long initiatorId, int from, int size);

    /**
     * Performs a complex search for events based on public-facing criteria.
     *
     * @param params The public search parameters.
     * @param from   The starting index.
     * @param size   The number of events to return.
     * @return A list of matching events.
     */
    List<Event> findPublic(PublicEventSearchParams params, int from, int size);

    /**
     * Performs a complex search for events based on admin-level criteria.
     *
     * @param params The admin search parameters.
     * @param from   The starting index.
     * @param size   The number of events to return.
     * @return A list of matching events.
     */
    List<Event> findAdmin(AdminEventSearchParams params, int from, int size);

    /**
     * Checks if any events are associated with a given category ID.
     *
     * @param categoryId The ID of the category to check.
     * @return true if events exist for the category, false otherwise.
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Finds all events whose IDs are in the given set.
     * This is useful for bulk fetching of specific events.
     *
     * @param eventIds A set of event IDs to find.
     * @return A list of found events. The list may be smaller than the input set if some IDs were not found.
     */
    List<Event> findAllByIdIn(Set<Long> eventIds);
}