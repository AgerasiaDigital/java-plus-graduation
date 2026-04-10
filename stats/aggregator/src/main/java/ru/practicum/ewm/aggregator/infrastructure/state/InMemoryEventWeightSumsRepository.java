package ru.practicum.ewm.aggregator.infrastructure.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.aggregator.domain.EventWeightSumsRepository;

@Repository
public class InMemoryEventWeightSumsRepository implements EventWeightSumsRepository {

    private final Map<Long, Double> eventWeightSums = new ConcurrentHashMap<>();

    @Override
    public double findWeightSum(long eventId) {
        return eventWeightSums.getOrDefault(eventId, 0.0);
    }

    @Override
    public void saveWeightSum(long eventId, double sum) {
        eventWeightSums.put(eventId, sum);
    }

    @Override
    public Map<Long, Double> findWeightSums(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return eventIds.stream()
            .collect(Collectors.toConcurrentMap( // Use a concurrent map for thread safety
                Function.identity(),
                eventId -> eventWeightSums.getOrDefault(eventId, 0.0) // Provide 0 for missing values
            ));
    }
}