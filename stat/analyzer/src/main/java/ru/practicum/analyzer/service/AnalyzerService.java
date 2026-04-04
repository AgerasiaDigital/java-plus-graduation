package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserEventWeight;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserEventWeightRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyzerService {

    private static final int K_NEIGHBORS = 10;

    private final UserEventWeightRepository weightRepository;
    private final EventSimilarityRepository similarityRepository;

    private static final Map<String, Double> ACTION_WEIGHTS = Map.of(
            "VIEW", 0.4,
            "REGISTER", 0.8,
            "LIKE", 1.0
    );

    public void processUserAction(long userId, long eventId, String actionType, long timestampMs) {
        double weight = ACTION_WEIGHTS.getOrDefault(actionType, 0.4);
        weightRepository.findByUserIdAndEventId(userId, eventId)
                .ifPresentOrElse(existing -> {
                    if (weight > existing.getMaxWeight()) {
                        existing.setMaxWeight(weight);
                    }
                    existing.setLastInteractionTs(timestampMs);
                    weightRepository.save(existing);
                }, () -> {
                    UserEventWeight w = new UserEventWeight();
                    w.setUserId(userId);
                    w.setEventId(eventId);
                    w.setMaxWeight(weight);
                    w.setLastInteractionTs(timestampMs);
                    weightRepository.save(w);
                });
    }

    public void processSimilarity(long eventA, long eventB, double score) {
        long e1 = Math.min(eventA, eventB);
        long e2 = Math.max(eventA, eventB);
        similarityRepository.findByEventAAndEventB(e1, e2)
                .ifPresentOrElse(existing -> {
                    existing.setScore(score);
                    similarityRepository.save(existing);
                }, () -> {
                    EventSimilarity sim = new EventSimilarity();
                    sim.setEventA(e1);
                    sim.setEventB(e2);
                    sim.setScore(score);
                    similarityRepository.save(sim);
                });
    }

    /**
     * Algorithm:
     * 1. Get user's recently interacted events (sorted newest first, limit maxResults)
     * 2. For each, find similar events user hasn't interacted with → candidates
     * 3. Pick top maxResults candidates by similarity
     * 4. For each candidate, predict score using KNN:
     *    predicted = sum(sim(candidate, interacted) * user_weight(interacted)) / sum(sim values)
     */
    @Transactional(readOnly = true)
    public List<long[]> getRecommendationsForUser(long userId, int maxResults) {
        int limit = maxResults > 0 ? maxResults : 10;

        List<UserEventWeight> userHistory = weightRepository.findByUserIdOrderByTsDesc(userId);
        if (userHistory.isEmpty()) return List.of();

        Set<Long> interactedIds = userHistory.stream()
                .map(UserEventWeight::getEventId)
                .collect(Collectors.toSet());
        Map<Long, Double> interactedWeights = userHistory.stream()
                .collect(Collectors.toMap(UserEventWeight::getEventId, UserEventWeight::getMaxWeight));

        // Collect candidates and their best similarity to any interacted event
        Map<Long, Double> candidateBestSim = new HashMap<>();
        for (UserEventWeight interaction : userHistory) {
            List<EventSimilarity> sims = similarityRepository.findByEvent(interaction.getEventId());
            for (EventSimilarity sim : sims) {
                long candidate = sim.getEventA().equals(interaction.getEventId())
                        ? sim.getEventB() : sim.getEventA();
                if (interactedIds.contains(candidate)) continue;
                candidateBestSim.merge(candidate, sim.getScore(), Math::max);
            }
        }

        if (candidateBestSim.isEmpty()) return List.of();

        // Pick top candidates by best similarity
        List<Long> topCandidates = candidateBestSim.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        // Compute predicted score for each candidate using KNN
        List<long[]> result = new ArrayList<>();
        for (Long candidate : topCandidates) {
            List<EventSimilarity> sims = similarityRepository.findByEvent(candidate);

            // K nearest neighbors = top K most similar interacted events
            List<double[]> neighbors = sims.stream()
                    .map(sim -> {
                        long neighbor = sim.getEventA().equals(candidate) ? sim.getEventB() : sim.getEventA();
                        return new double[]{neighbor, sim.getScore()};
                    })
                    .filter(pair -> interactedIds.contains((long) pair[0]))
                    .sorted((a, b) -> Double.compare(b[1], a[1]))
                    .limit(K_NEIGHBORS)
                    .toList();

            if (neighbors.isEmpty()) continue;

            double weightedSum = 0.0;
            double simSum = 0.0;
            for (double[] neighbor : neighbors) {
                long neighborId = (long) neighbor[0];
                double simScore = neighbor[1];
                double userWeight = interactedWeights.getOrDefault(neighborId, 0.0);
                weightedSum += simScore * userWeight;
                simSum += simScore;
            }

            if (simSum == 0.0) continue;
            double predictedScore = weightedSum / simSum;
            result.add(new long[]{candidate, Double.doubleToLongBits(predictedScore)});
        }

        result.sort((a, b) -> Double.compare(
                Double.longBitsToDouble(b[1]),
                Double.longBitsToDouble(a[1])));
        return result;
    }

    /**
     * Get events similar to eventId that userId hasn't interacted with.
     */
    @Transactional(readOnly = true)
    public List<long[]> getSimilarEvents(long eventId, long userId, int maxResults) {
        int limit = maxResults > 0 ? maxResults : 10;

        Set<Long> interactedIds = weightRepository.findByUserId(userId).stream()
                .map(UserEventWeight::getEventId)
                .collect(Collectors.toSet());

        return similarityRepository.findByEvent(eventId).stream()
                .map(sim -> {
                    long candidate = sim.getEventA().equals(eventId) ? sim.getEventB() : sim.getEventA();
                    return new long[]{candidate, Double.doubleToLongBits(sim.getScore())};
                })
                .filter(pair -> !interactedIds.contains(pair[0]))
                .limit(limit)
                .toList();
    }

    /**
     * For each event, sum max_weights across all users (the "rating").
     */
    @Transactional(readOnly = true)
    public List<long[]> getInteractionsCount(List<Long> eventIds) {
        List<long[]> result = new ArrayList<>();
        for (Long eventId : eventIds) {
            Double sum = weightRepository.sumMaxWeightByEventId(eventId);
            result.add(new long[]{eventId, Double.doubleToLongBits(sum != null ? sum : 0.0)});
        }
        return result;
    }
}
