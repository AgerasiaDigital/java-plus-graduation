package ru.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SimilarityCalculator {

    // event_id -> (user_id -> max_weight)
    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    // event_id -> sum of max_weights across all users
    private final Map<Long, Double> eventSums = new HashMap<>();
    // ordered pair (min_id, max_id) -> S_min sum
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    private static final Map<String, Double> ACTION_WEIGHTS = Map.of(
            "VIEW", 0.4,
            "REGISTER", 0.8,
            "LIKE", 1.0
    );

    /**
     * Processes a user action, updates internal state and returns updated similarities.
     */
    public synchronized List<EventSimilarityAvro> processAction(long userId, long eventId,
                                                                 String actionType, long timestampMs) {
        double newWeight = ACTION_WEIGHTS.getOrDefault(actionType, 0.4);

        Map<Long, Double> usersForEvent = eventUserWeights.computeIfAbsent(eventId, e -> new HashMap<>());
        double oldWeight = usersForEvent.getOrDefault(userId, 0.0);

        // Only max weight counts per user-event pair
        if (newWeight <= oldWeight) {
            return List.of();
        }

        double weightDelta = newWeight - oldWeight;
        usersForEvent.put(userId, newWeight);
        eventSums.merge(eventId, weightDelta, Double::sum);

        // Update S_min for all pairs (eventId, otherEventId) where user interacted with both
        List<EventSimilarityAvro> results = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            double userWeightForOther = entry.getValue().getOrDefault(userId, 0.0);
            if (userWeightForOther == 0.0) continue; // user hasn't interacted with other event

            double oldMin = Math.min(oldWeight, userWeightForOther);
            double newMin = Math.min(newWeight, userWeightForOther);
            double minDelta = newMin - oldMin;

            if (minDelta == 0.0) continue;

            double currentSmin = getMinWeightSum(eventId, otherEventId);
            double updatedSmin = currentSmin + minDelta;
            putMinWeightSum(eventId, otherEventId, updatedSmin);

            double sA = eventSums.getOrDefault(eventId, 0.0);
            double sB = eventSums.getOrDefault(otherEventId, 0.0);
            double denominator = Math.sqrt(sA) * Math.sqrt(sB);
            if (denominator == 0.0) continue;

            double similarity = updatedSmin / denominator;
            long eA = Math.min(eventId, otherEventId);
            long eB = Math.max(eventId, otherEventId);

            results.add(EventSimilarityAvro.newBuilder()
                    .setEventA(eA)
                    .setEventB(eB)
                    .setScore(similarity)
                    .setTimestamp(java.time.Instant.ofEpochMilli(timestampMs))
                    .build());
        }

        return results;
    }

    private double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }
}
