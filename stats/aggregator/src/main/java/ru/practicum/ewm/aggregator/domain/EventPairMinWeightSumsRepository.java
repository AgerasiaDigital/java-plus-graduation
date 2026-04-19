package ru.practicum.ewm.aggregator.domain;

import java.util.Map;

public interface EventPairMinWeightSumsRepository {

    /**
     * Updates the aggregated sums of minimum weights between a primary event and a set of other
     * events, applying specified delta values for each pair. For each entry in the {@code deltas}
     * map, the method updates the sum of minimum weights for the pair consisting of {@code eventId}
     * and the key from the {@code deltas} map, incrementing it by the corresponding delta value.
     *
     * @param eventId The ID of the primary event.
     * @param deltas  A {@link Map} where keys are the IDs of the other events to form pairs with
     *                {@code eventId}, and values are the delta amounts to add to the current
     *                aggregated sum for each pair.
     * @return A {@link Map} where keys are the IDs of the other events and values are their
     * corresponding *updated* aggregated sums of minimum weights with {@code eventId}.
     */
    Map<Long, Double> updateWithDeltas(long eventId, Map<Long, Double> deltas);
}