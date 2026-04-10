package ru.practicum.ewm.aggregator.infrastructure.state;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.aggregator.domain.UserEventWeightsRepository;

@Repository
public class InMemoryUserEventWeightsRepository implements UserEventWeightsRepository {

    private final Map<Long, Map<Long, Double>> userEventWeights = new ConcurrentHashMap<>();

    @Override
    public double findWeight(long userId, long eventId) {
        Map<Long, Double> eventWeights = userEventWeights.get(userId);
        if (eventWeights == null) {
            return 0.0; // User has no interactions, so weight is 0
        }
        return eventWeights.getOrDefault(eventId, 0.0);
    }

    @Override
    public void save(long userId, long eventId, double weight) {
        userEventWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(eventId, weight);
    }

    @Override
    public Map<Long, Double> findWeightsByUserId(long userId) {
        return Collections.unmodifiableMap(userEventWeights.getOrDefault(userId, Collections.emptyMap()));
    }
}