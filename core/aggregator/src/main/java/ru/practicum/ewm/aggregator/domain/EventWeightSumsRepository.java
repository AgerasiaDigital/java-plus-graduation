package ru.practicum.ewm.aggregator.domain;

import java.util.Map;
import java.util.Set;

public interface EventWeightSumsRepository {

    /**
     * Retrieves the total sum of weights for a given event across all users. This sum represents
     * the aggregation of individual user weights for the specified event.
     *
     * @param eventId The ID of the event for which to retrieve the total weight sum.
     * @return The aggregated sum of weights for the specified event across all users.
     */
    double findWeightSum(long eventId);

    /**
     * Saves or updates the total sum of weights for a given event across all users. This method
     * stores the pre-calculated aggregate sum of weights for the specified event.
     *
     * @param eventId The ID of the event for which to save or update the total weight sum.
     * @param sum     The calculated total sum of weights for the event across all users.
     */
    void saveWeightSum(long eventId, double sum);

    /**
     * Retrieves the total sums of weights for a given set of events across all users. The returned
     * map contains event IDs as keys and their corresponding aggregated total weight sums as
     * values.
     *
     * @param eventIds A {@link Set} of event IDs for which to retrieve the total weight sums.
     * @return A {@link Map} where keys are event IDs and values are their respective aggregated
     * total weight sums across all users.
     */
    Map<Long, Double> findWeightSums(Set<Long> eventIds);
}