package ru.practicum.ewm.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventSimilarityCalculator {

    // eventId -> (userId -> maxWeight)
    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();

    // eventId -> S_event (sum of max weights per user)
    private final Map<Long, Double> eventWeightsSums = new HashMap<>();

    // min(eventA, eventB) -> max(eventA, eventB) -> S_min
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> processUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = toWeight(action.getActionType());

        Map<Long, Double> usersForEvent = eventWeights.computeIfAbsent(eventId, k -> new HashMap<>());
        double oldWeight = usersForEvent.getOrDefault(userId, 0.0);

        // Only update if weight increases (max weight rule)
        if (newWeight <= oldWeight) {
            return List.of();
        }

        double delta = newWeight - oldWeight;
        usersForEvent.put(userId, newWeight);

        // Update S_event
        eventWeightsSums.merge(eventId, delta, Double::sum);

        // Update S_min for all pairs (eventId, otherEvent) where user interacted with both
        for (Map.Entry<Long, Map<Long, Double>> entry : eventWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            Double userWeightForOther = entry.getValue().get(userId);
            if (userWeightForOther == null) {
                // User hasn't interacted with otherEvent — no contribution to S_min
                continue;
            }

            // Old contribution: min(oldWeight, userWeightForOther)
            // New contribution: min(newWeight, userWeightForOther)
            double oldContrib = Math.min(oldWeight, userWeightForOther);
            double newContrib = Math.min(newWeight, userWeightForOther);
            double minDelta = newContrib - oldContrib;

            if (minDelta != 0) {
                double current = getMinWeightsSum(eventId, otherEventId);
                putMinWeightsSum(eventId, otherEventId, current + minDelta);
            }
        }

        // Recalculate and emit similarity for all events this user has interacted with
        Instant now = Instant.now();
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            if (entry.getValue().get(userId) == null) {
                // User didn't interact with otherEvent — similarity unchanged
                continue;
            }

            double sMin = getMinWeightsSum(eventId, otherEventId);
            double sEvent = eventWeightsSums.getOrDefault(eventId, 0.0);
            double sOther = eventWeightsSums.getOrDefault(otherEventId, 0.0);

            if (sEvent == 0 || sOther == 0) continue;

            double sim = sMin / (Math.sqrt(sEvent) * Math.sqrt(sOther));
            if (sim <= 0) continue;

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);

            similarities.add(EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(sim)
                    .setTimestamp(now)
                    .build());

            log.debug("Similarity({},{})={}", eventA, eventB, sim);
        }

        return similarities;
    }

    private double getMinWeightsSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void putMinWeightsSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .put(second, sum);
    }

    private double toWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.7;
            case LIKE -> 1.0;
        };
    }
}
