package ru.practicum.ewm.analyzer.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventSimilarityRepository {

    /**
     * Retrieves event similarity data for a specific pair of events.
     * The method considers the pair (eventA, eventB) regardless of the order, typically by
     * normalizing the event IDs internally (e.g., using {@code Math.min} and {@code Math.max}).
     *
     * @param eventA The ID of the first event in the pair.
     * @param eventB The ID of the second event in the pair.
     * @return An {@link Optional} containing the {@link EventSimilarity} object if data for the pair is found,
     *         or an empty {@link Optional} otherwise.
     */
    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);

    /**
     * Saves a new event similarity record or updates an existing one.
     *
     * @param similarity The {@link EventSimilarity} object to be saved.
     * @return The saved {@link EventSimilarity} object, possibly with generated IDs or updated fields.
     */
    EventSimilarity save(EventSimilarity similarity);

    /**
     * Retrieves a specified number of events that are most similar to a target event,
     * excluding any events provided in the exclusion list. The results are ordered by similarity score.
     *
     * @param eventId The ID of the primary event for which similar events are sought.
     * @param excludedEvents A {@link Collection} of event IDs that should be excluded from the search results.
     * @param maxResults The maximum number of similar events to return.
     * @return A {@link List} of {@link Recommendation} objects, representing the top similar events
     *         that are not in the excluded list, up to {@code maxResults}.
     */
    List<Recommendation> findTopSimilarExcluding(long eventId, Collection<Long> excludedEvents, int maxResults);

    /**
     * Retrieves the `maxResults` number of top events based on their average similarity to a given set of events.
     * The returned list is ordered by the average similarity score in descending order.
     *
     * @param recentlyInteractedEvents A {@link Collection} of event IDs representing the base set for similarity calculation.
     * @param maxResults The maximum number of top similar events to return.
     * @return A {@link List} of {@code Long} representing the IDs of the top similar events, ordered by average similarity.
     */
    List<Long> findTopSimilarToSet(Collection<Long> recentlyInteractedEvents, int maxResults);

    /**
     * Retrieves at most `maxNeighbours` most similar events from a list of candidate events
     * for each of the given set of primary events.
     * The result maps each primary event ID to a list of its most similar "neighbor" events
     * (with their similarity scores) found within the candidate pool, ordered by similarity.
     *
     * @param primaryEvents A {@link Collection} of event IDs for which to find neighbor events.
     * @param candidates A {@link Collection} of event IDs from which the neighbor events should be selected.
     * @param maxNeighbours The maximum number of neighbor events to retrieve for each primary event.
     * @return A {@link Map} where keys are event IDs from {@code primaryEvents} and values are
     *         {@link List}s of {@link Recommendation} objects, representing their top similar
     *         neighbors from the {@code candidates} collection.
     */
    Map<Long, List<Recommendation>> findNeighbourEventsFrom(Collection<Long> primaryEvents, Collection<Long> candidates, int maxNeighbours);
}