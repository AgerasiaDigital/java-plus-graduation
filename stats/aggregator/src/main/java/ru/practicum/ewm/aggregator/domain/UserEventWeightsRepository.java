package ru.practicum.ewm.aggregator.domain;

import java.util.Map;

public interface UserEventWeightsRepository {

    /**
     * Retrieves the current maximum weight associated with a specific user and event.
     *
     * @param userId  The ID of the user.
     * @param eventId The ID of the event.
     * @return The maximum weight if found, 0 otherwise.
     */
    double findWeight(long userId, long eventId);

    /**
     * Saves or updates the maximum weight for a specific user and event. If a weight for the given
     * user and event already exists, it will be updated.
     *
     * @param userId  The ID of the user.
     * @param eventId The ID of the event.
     * @param weight  The new maximum weight to save.
     */
    void save(long userId, long eventId, double weight);

    /**
     * Retrieves the weights for all events that a specific user has interacted with. The returned
     * map contains event IDs as keys and the user's corresponding maximum weights as values.
     *
     * @param userId The ID of the user for whom to retrieve event weights.
     * @return A {@link Map} where keys are event IDs and values are the user's respective maximum
     * weights for those events.
     */
    Map<Long, Double> findWeightsByUserId(long userId);
}
