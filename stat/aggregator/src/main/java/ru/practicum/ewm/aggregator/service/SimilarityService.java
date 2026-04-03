package ru.practicum.ewm.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SimilarityService {

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 1.0,
            ActionTypeAvro.REGISTER, 5.0,
            ActionTypeAvro.LIKE, 10.0
    );

    // eventId -> userId -> maxWeight
    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();
    // eventId -> sum of max weights of all users
    private final Map<Long, Double> eventTotalWeights = new HashMap<>();
    // min(eventA, eventB) -> max(eventA, eventB) -> S_min
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> processUserAction(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        double weight = ACTION_WEIGHTS.getOrDefault(action.getActionType(), 1.0);

        Map<Long, Double> userWeights = eventWeights.computeIfAbsent(eventId, k -> new HashMap<>());
        double oldWeight = userWeights.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, weight);

        if (Double.compare(newWeight, oldWeight) == 0) {
            return List.of();
        }

        userWeights.put(userId, newWeight);

        double oldTotal = eventTotalWeights.getOrDefault(eventId, 0.0);
        eventTotalWeights.put(eventId, oldTotal - oldWeight + newWeight);

        List<Long> otherEventIds = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : eventWeights.entrySet()) {
            long otherId = entry.getKey();
            if (otherId != eventId && entry.getValue().containsKey(userId)) {
                otherEventIds.add(otherId);
            }
        }

        if (otherEventIds.isEmpty()) {
            return List.of();
        }

        List<EventSimilarityAvro> similarities = new ArrayList<>();
        double newTotal = eventTotalWeights.get(eventId);

        for (long otherEventId : otherEventIds) {
            double otherWeight = eventWeights.get(otherEventId).get(userId);
            double oldContrib = Math.min(oldWeight, otherWeight);
            double newContrib = Math.min(newWeight, otherWeight);

            double oldSMin = getMinWeightsSum(eventId, otherEventId);
            double newSMin = oldSMin - oldContrib + newContrib;
            putMinWeightsSum(eventId, otherEventId, newSMin);

            double sOther = eventTotalWeights.getOrDefault(otherEventId, 0.0);
            double score = computeSimilarity(newSMin, newTotal, sOther);

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);

            similarities.add(EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(score)
                    .setTimestamp(action.getTimestamp())
                    .build());
        }

        return similarities;
    }

    private double computeSimilarity(double sMin, double sA, double sB) {
        if (sA <= 0 || sB <= 0) return 0.0;
        return sMin / (Math.sqrt(sA) * Math.sqrt(sB));
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
}
