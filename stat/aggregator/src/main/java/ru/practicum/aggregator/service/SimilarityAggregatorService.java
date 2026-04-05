package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimilarityAggregatorService {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    private final Environment environment;

    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();

    public synchronized void onUserAction(UserActionAvro action) {
        double weight = weightOf(action.getActionType());
        long userId = action.getUserId();
        long eventId = action.getEventId();
        long ts = action.getTimestamp().toEpochMilli();

        boolean newEvent = !eventUserWeights.containsKey(eventId);
        Set<Long> existingOthers = new HashSet<>(eventSums.keySet());

        Map<Long, Double> usersForEvent = eventUserWeights.computeIfAbsent(eventId, k -> new HashMap<>());
        double oldMax = usersForEvent.getOrDefault(userId, 0.0);
        if (weight <= oldMax) {
            return;
        }

        usersForEvent.put(userId, weight);
        double deltaSum = weight - oldMax;
        eventSums.merge(eventId, deltaSum, Double::sum);

        String topic = environment.getRequiredProperty("ewm.kafka.topic.events-similarity");

        if (newEvent) {
            for (Long other : existingOthers) {
                double wOther = eventUserWeights.getOrDefault(other, Map.of()).getOrDefault(userId, 0.0);
                double sMin = Math.min(weight, wOther);
                putMin(eventId, other, sMin);
                sendSimilarity(topic, eventId, other, ts);
            }
        } else {
            for (Long other : eventSums.keySet()) {
                if (other.equals(eventId)) {
                    continue;
                }
                double wOther = eventUserWeights.getOrDefault(other, Map.of()).getOrDefault(userId, 0.0);
                double deltaMin = Math.min(weight, wOther) - Math.min(oldMax, wOther);
                if (deltaMin != 0) {
                    addMin(eventId, other, deltaMin);
                }
                sendSimilarity(topic, eventId, other, ts);
            }
        }
    }

    private void sendSimilarity(String topic, long eventA, long eventB, long timestampMillis) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        double sMin = getMin(first, second);
        double sA = eventSums.getOrDefault(first, 0.0);
        double sB = eventSums.getOrDefault(second, 0.0);
        double score = 0.0;
        if (sA > 0 && sB > 0) {
            score = sMin / (Math.sqrt(sA) * Math.sqrt(sB));
        }
        EventSimilarityAvro out = new EventSimilarityAvro();
        out.setEventA(first);
        out.setEventB(second);
        out.setScore(score);
        out.setTimestamp(Instant.ofEpochMilli(timestampMillis));
        kafkaTemplate.send(topic, first + "-" + second, out);
    }

    private void putMin(long eventA, long eventB, double value) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, value);
    }

    private void addMin(long eventA, long eventB, double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        Map<Long, Double> row = minWeightSums.computeIfAbsent(first, k -> new HashMap<>());
        row.merge(second, delta, Double::sum);
    }

    private double getMin(long first, long second) {
        return minWeightSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private static double weightOf(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 1.0;
            case REGISTER -> 2.0;
            case LIKE -> 3.0;
        };
    }
}
