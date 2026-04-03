package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.UserAction;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.proto.analyzer.RecommendedEventProto;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int KNN = 10;

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        List<UserAction> recentInteractions = userActionRepository
                .findByIdUserIdOrderByTimestampDesc(userId, PageRequest.of(0, maxResults));

        if (recentInteractions.isEmpty()) {
            return Stream.empty();
        }

        Set<Long> interactedIds = new HashSet<>();
        Map<Long, Double> userRatings = new HashMap<>();
        for (UserAction ua : recentInteractions) {
            interactedIds.add(ua.getId().getEventId());
            userRatings.put(ua.getId().getEventId(), ua.getWeight());
        }

        List<Long> interactedList = new ArrayList<>(interactedIds);
        List<EventSimilarity> similarities = eventSimilarityRepository.findByEventIn(interactedList);

        Map<Long, Double> candidateMaxSim = new HashMap<>();
        for (EventSimilarity sim : similarities) {
            long a = sim.getEventA();
            long b = sim.getEventB();
            if (interactedIds.contains(a) && !interactedIds.contains(b)) {
                candidateMaxSim.merge(b, sim.getScore(), Math::max);
            } else if (interactedIds.contains(b) && !interactedIds.contains(a)) {
                candidateMaxSim.merge(a, sim.getScore(), Math::max);
            }
        }

        List<Long> topCandidates = candidateMaxSim.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        if (topCandidates.isEmpty()) {
            return Stream.empty();
        }

        return topCandidates.stream().map(candidateId -> {
            List<EventSimilarity> knnSims = similarities.stream()
                    .filter(s -> {
                        long a = s.getEventA(), b = s.getEventB();
                        return (a == candidateId && interactedIds.contains(b))
                                || (b == candidateId && interactedIds.contains(a));
                    })
                    .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                    .limit(KNN)
                    .toList();

            double sumWeighted = 0.0, sumSims = 0.0;
            for (EventSimilarity s : knnSims) {
                long neighborId = s.getEventA() == candidateId ? s.getEventB() : s.getEventA();
                double rating = userRatings.getOrDefault(neighborId, 0.0);
                sumWeighted += s.getScore() * rating;
                sumSims += s.getScore();
            }

            float score = sumSims > 0 ? (float) (sumWeighted / sumSims) : 0f;
            return RecommendedEventProto.newBuilder()
                    .setEventId(candidateId)
                    .setScore(score)
                    .build();
        });
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        List<EventSimilarity> similarities = eventSimilarityRepository.findByEvent(eventId);

        Set<Long> userInteracted = new HashSet<>();
        userActionRepository.findByIdUserId(userId)
                .forEach(ua -> userInteracted.add(ua.getId().getEventId()));

        return similarities.stream()
                .filter(s -> {
                    long a = s.getEventA(), b = s.getEventB();
                    long other = (a == eventId) ? b : a;
                    return !userInteracted.contains(other);
                })
                .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                .limit(maxResults)
                .map(s -> {
                    long other = (s.getEventA() == eventId) ? s.getEventB() : s.getEventA();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(other)
                            .setScore((float) s.getScore())
                            .build();
                });
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Stream.empty();
        List<Object[]> rows = userActionRepository.sumWeightsByEventIds(eventIds);
        return rows.stream().map(row -> RecommendedEventProto.newBuilder()
                .setEventId((Long) row[0])
                .setScore(((Double) row[1]).floatValue())
                .build());
    }
}
