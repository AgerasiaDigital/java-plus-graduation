package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserEventInteraction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserEventInteractionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationsService {

    private final UserEventInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Value("${ewm.recommendations.recent-interactions-limit:10}")
    private int recentInteractionsLimit;

    @Value("${ewm.recommendations.nearest-neighbors-k:5}")
    private int nearestNeighborsK;

    @Transactional(readOnly = true)
    public List<ScoredEvent> getInteractionsCount(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return List.of();
        }
        List<Object[]> rows = interactionRepository.sumMaxWeightsForEvents(eventIds);
        Map<Long, Double> sums = new HashMap<>();
        for (Object[] row : rows) {
            sums.put((Long) row[0], ((Number) row[1]).doubleValue());
        }
        List<ScoredEvent> out = new ArrayList<>();
        for (Long id : eventIds) {
            out.add(new ScoredEvent(id, sums.getOrDefault(id, 0.0)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<ScoredEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        List<EventSimilarity> rows = similarityRepository.findAllInvolvingEvent(eventId);
        List<ScoredEvent> candidates = new ArrayList<>();
        for (EventSimilarity s : rows) {
            long other = otherEvent(s, eventId);
            if (interactionRepository.existsByUserIdAndEventId(userId, eventId)
                    && interactionRepository.existsByUserIdAndEventId(userId, other)) {
                continue;
            }
            candidates.add(new ScoredEvent(other, s.getScore()));
        }
        candidates.sort(Comparator.comparingDouble(ScoredEvent::score).reversed());
        return candidates.stream().limit(maxResults).toList();
    }

    @Transactional(readOnly = true)
    public List<ScoredEvent> getRecommendationsForUser(long userId, int maxResults) {
        List<UserEventInteraction> recent = interactionRepository.findByUserIdOrderByLastInteractionAtDesc(
                userId, PageRequest.of(0, recentInteractionsLimit));
        if (recent.isEmpty()) {
            return List.of();
        }
        Set<Long> allUserEvents = new HashSet<>(interactionRepository.findEventIdsByUserId(userId));
        Map<Long, Double> candidateBestSim = new HashMap<>();
        for (UserEventInteraction ri : recent) {
            long e = ri.getEventId();
            for (EventSimilarity s : similarityRepository.findAllInvolvingEvent(e)) {
                long other = otherEvent(s, e);
                if (allUserEvents.contains(other)) {
                    continue;
                }
                candidateBestSim.merge(other, s.getScore(), Math::max);
            }
        }
        List<Map.Entry<Long, Double>> bySim = candidateBestSim.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .toList();

        List<ScoredEvent> predictions = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : bySim) {
            long candidate = entry.getKey();
            List<Neighbor> neighbors = new ArrayList<>();
            for (UserEventInteraction ri : recent) {
                long e = ri.getEventId();
                Optional<EventSimilarity> simRow = findSimilarity(e, candidate);
                if (simRow.isEmpty() || simRow.get().getScore() <= 0) {
                    continue;
                }
                neighbors.add(new Neighbor(e, simRow.get().getScore()));
            }
            neighbors.sort(Comparator.comparingDouble(Neighbor::sim).reversed());
            List<Neighbor> topK = neighbors.stream().limit(nearestNeighborsK).toList();
            if (topK.isEmpty()) {
                continue;
            }
            double num = 0.0;
            double den = 0.0;
            for (Neighbor n : topK) {
                double w = interactionRepository.findByUserIdAndEventId(userId, n.eventId())
                        .map(UserEventInteraction::getMaxWeight)
                        .orElse(0.0);
                num += n.sim() * w;
                den += n.sim();
            }
            if (den <= 0) {
                continue;
            }
            predictions.add(new ScoredEvent(candidate, num / den));
        }
        predictions.sort(Comparator.comparingDouble(ScoredEvent::score).reversed());
        return predictions.stream().limit(maxResults).toList();
    }

    @Transactional(readOnly = true)
    public double getUserEventMaxWeight(long userId, long eventId) {
        return interactionRepository.findByUserIdAndEventId(userId, eventId)
                .map(UserEventInteraction::getMaxWeight)
                .orElse(0.0);
    }

    @Transactional
    public void mergeUserAction(long userId, long eventId, double weight, java.time.Instant at) {
        Optional<UserEventInteraction> opt = interactionRepository.findByUserIdAndEventId(userId, eventId);
        if (opt.isEmpty()) {
            UserEventInteraction u = new UserEventInteraction();
            u.setUserId(userId);
            u.setEventId(eventId);
            u.setMaxWeight(weight);
            u.setLastInteractionAt(at);
            interactionRepository.save(u);
            return;
        }
        UserEventInteraction u = opt.get();
        if (weight > u.getMaxWeight()) {
            u.setMaxWeight(weight);
            u.setLastInteractionAt(at);
            interactionRepository.save(u);
        } else if (weight == u.getMaxWeight()) {
            u.setLastInteractionAt(at);
            interactionRepository.save(u);
        }
    }

    @Transactional
    public void mergeEventSimilarity(long eventA, long eventB, double score, java.time.Instant updatedAt) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        EventSimilarity row = similarityRepository.findByEventAAndEventB(first, second)
                .orElseGet(EventSimilarity::new);
        row.setEventA(first);
        row.setEventB(second);
        row.setScore(score);
        row.setUpdatedAt(updatedAt);
        similarityRepository.save(row);
    }

    private Optional<EventSimilarity> findSimilarity(long x, long y) {
        long a = Math.min(x, y);
        long b = Math.max(x, y);
        return similarityRepository.findByEventAAndEventB(a, b);
    }

    private static long otherEvent(EventSimilarity s, long eventId) {
        return s.getEventA() == eventId ? s.getEventB() : s.getEventA();
    }

    public record ScoredEvent(long eventId, double score) {
    }

    private record Neighbor(long eventId, double sim) {
    }
}
