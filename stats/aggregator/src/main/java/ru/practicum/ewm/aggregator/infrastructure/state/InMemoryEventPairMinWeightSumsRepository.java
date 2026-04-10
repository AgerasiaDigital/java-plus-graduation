package ru.practicum.ewm.aggregator.infrastructure.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.aggregator.domain.EventPairMinWeightSumsRepository;

@Repository
public class InMemoryEventPairMinWeightSumsRepository implements EventPairMinWeightSumsRepository {

    private final Map<Long, Map<Long, Double>> minWeightSums = new ConcurrentHashMap<>();

    @Override
    public Map<Long, Double> updateWithDeltas(long eventId, Map<Long, Double> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> updatedSums = new ConcurrentHashMap<>();

        for (Map.Entry<Long, Double> entry : deltas.entrySet()) {
            long otherEventId = entry.getKey();
            double delta = entry.getValue();

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);

            Map<Long, Double> innerMap = minWeightSums.computeIfAbsent(eventA, k -> new ConcurrentHashMap<>());

            double newSum = innerMap.compute(eventB, (key, currentSum) -> {
                if (currentSum == null) {
                    return delta; // First time seeing this pair
                }
                return currentSum + delta; // Add delta to existing sum
            });

            updatedSums.put(otherEventId, newSum);
        }

        return updatedSums;
    }
}