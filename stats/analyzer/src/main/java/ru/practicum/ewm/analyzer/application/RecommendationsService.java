package ru.practicum.ewm.analyzer.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.application.config.RecommendationProperties;
import ru.practicum.ewm.analyzer.domain.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.domain.Recommendation;
import ru.practicum.ewm.analyzer.domain.UserInteractionRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationsService {

    private final EventSimilarityRepository similarityRepository;
    private final UserInteractionRepository interactionRepository;

    private final RecommendationProperties properties;

    @Transactional(readOnly = true)
    public List<Recommendation> findSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("Finding top {} similar events for event {} excluding events interacted by user {}", maxResults, eventId, userId);

        List<Long> interactedEvents = interactionRepository.findInteractedEvents(userId);
        log.debug("User {} has interacted with {} events.", userId, interactedEvents.size());

        List<Recommendation> similarEvents = similarityRepository.findTopSimilarExcluding(eventId, interactedEvents, maxResults);
        log.info("Found {} similar events for event {} excluding user {}'s interactions.", similarEvents.size(), eventId, userId);
        return similarEvents;
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getInteractionsCount(Collection<Long> eventIds) {
        log.info("Finding interaction counts for {} events.", eventIds.size());

        List<Recommendation> interactionCounts = interactionRepository.findInteractionsCount(eventIds);
        log.info("Retrieved interaction counts for {} events.", interactionCounts.size());
        return interactionCounts;
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getUserPredictions(long userId, int maxResults) {
        log.info("Generating top {} event predictions for user {}.", maxResults, userId);

        // Select candidates for suggestion
        List<Long> interactedEvents = interactionRepository.findInteractedEvents(userId);
        List<Long> recentlyInteractedEvents = interactionRepository.findRecentlyInteractedEvents(
            userId, properties.getMaxRecentEventsForPrediction());
        log.debug("User {} recently interacted with {} events (max {}).", userId,
            recentlyInteractedEvents.size(), properties.getMaxRecentEventsForPrediction());

        List<Long> similarEvents = similarityRepository.findTopSimilarToSet(
            recentlyInteractedEvents, interactedEvents, maxResults);
        log.debug("Found {} candidate events similar to user {}'s recently interacted events (based on {} events, max {} results).",
            similarEvents.size(), userId, recentlyInteractedEvents.size(), maxResults);

        if (similarEvents.isEmpty()) {
            log.info("No similar events found as candidates for user {}. Returning empty predictions.", userId);
            return List.of();
        }

        // Find neighbours for candidates
        Map<Long, List<Recommendation>> neighbourEvents =
            similarityRepository.findNeighbourEventsFrom(
            similarEvents, interactedEvents, properties.getMaxNeighboursForPrediction());
        log.debug("Found neighbour events for {} candidate events with max {} neighbours each.",
            similarEvents.size(), properties.getMaxNeighboursForPrediction());

        // Collect all unique event IDs from neighbour events for fetching weights
        List<Long> neighbourEventIds = neighbourEvents.values().stream()
            .flatMap(List::stream)
            .map(Recommendation::getEventId)
            .collect(Collectors.toList());

        Map<Long, Double> neighbourUserWeights = interactionRepository.findInteractionWeights(userId,
            neighbourEventIds);

        // Normalize interaction weights to 0-1 user interest scores
        double maxActionWeight = properties.getActionWeights().values().stream()
            .mapToDouble(Double::doubleValue).max().orElse(1.0);

        // Calculate predicted scores based on neighbour scores
        List<Recommendation> predictions = similarEvents.stream().map(candidateEventId  -> {
            List<Recommendation> neighbours = neighbourEvents.getOrDefault(candidateEventId, List.of());

            // Filter out neighbours with invalid similarity scores (e.g. NaN/Infinity from Kafka)
            List<Recommendation> validNeighbours = neighbours.stream()
                .filter(n -> Double.isFinite(n.getScore()) && n.getScore() > 0)
                .toList();

            if (validNeighbours.isEmpty()) {
                log.debug("Candidate event {} has no valid neighbours. Predicted score will be 0.0.", candidateEventId);
                return new Recommendation(candidateEventId, 0.0);
            }

            // Calculate prediction for candidate using normalized user interest scores
            double weightedSumOfScores = validNeighbours.stream().mapToDouble(neighbour -> {
                double rawWeight = neighbourUserWeights.getOrDefault(neighbour.getEventId(), 0.0);
                double userScore = rawWeight / maxActionWeight; // normalize to [0, 1]
                return neighbour.getScore() * userScore;
            }).sum();
            double sumOfSimilarities = validNeighbours.stream().mapToDouble(Recommendation::getScore).sum();
            double predictedScore = (sumOfSimilarities == 0.0 || !Double.isFinite(weightedSumOfScores))
                ? 0.0 : weightedSumOfScores / sumOfSimilarities;
            if (!Double.isFinite(predictedScore)) {
                predictedScore = 0.0;
            }

            log.debug("Calculated predicted score for candidate event {}: {} (weightedSum: {}, sumSimilarities: {})",
                candidateEventId , predictedScore, weightedSumOfScores, sumOfSimilarities);
            return new Recommendation(candidateEventId , predictedScore);
        }).toList();

        log.info("Finished generating {} event predictions for user {}.", predictions.size(), userId);
        return predictions;
    }
}