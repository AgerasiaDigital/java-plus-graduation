package ru.practicum.ewm.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.analyzer.model.UserActionEntity;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;
import ru.practicum.grpc.stats.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.dashboard.RecommendedEventProto;
import ru.practicum.grpc.stats.dashboard.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.dashboard.UserEventWeightRequestProto;
import ru.practicum.grpc.stats.dashboard.UserPredictionsRequestProto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Value("${ewm.recommendations.default-max-results:10}")
    private int defaultMaxResults;

    @Value("${ewm.recommendations.recent-interactions-limit:10}")
    private int recentInteractionsLimit;

    @Value("${ewm.recommendations.nearest-neighbors-k:5}")
    private int nearestNeighborsK;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetRecommendationsForUser: userId={}, maxResults={}", request.getUserId(), request.getMaxResults());
        int limit = request.getMaxResults() > 0 ? request.getMaxResults() : defaultMaxResults;

        // Step 1: Get recent interactions sorted by date DESC, limit N
        List<UserActionEntity> recentActions = userActionRepository
                .findByUserIdOrderByTimestampDesc(request.getUserId(),
                        PageRequest.of(0, recentInteractionsLimit));

        if (recentActions.isEmpty()) {
            responseObserver.onCompleted();
            return;
        }

        Set<Long> interactedEvents = userActionRepository.findByUserId(request.getUserId())
                .stream().map(UserActionEntity::getEventId).collect(Collectors.toSet());

        // Step 2: Find candidate events — similar to recent events but not yet interacted with
        Map<Long, Double> candidateScores = new HashMap<>();
        for (UserActionEntity action : recentActions) {
            List<EventSimilarityEntity> similarities =
                    eventSimilarityRepository.findSimilarEvents(action.getEventId());
            for (EventSimilarityEntity sim : similarities) {
                long other = sim.getEventA().equals(action.getEventId()) ? sim.getEventB() : sim.getEventA();
                if (!interactedEvents.contains(other)) {
                    // Keep only the best similarity score per candidate
                    candidateScores.merge(other, sim.getScore(), Math::max);
                }
            }
        }

        if (candidateScores.isEmpty()) {
            responseObserver.onCompleted();
            return;
        }

        // Step 3: For each candidate compute predicted score via K-NN
        Map<Long, Double> userWeightsMap = recentActions.stream()
                .collect(Collectors.toMap(UserActionEntity::getEventId, UserActionEntity::getWeight));

        candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    long candidateEventId = entry.getKey();
                    double score = computePredictedScore(candidateEventId, userWeightsMap);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(candidateEventId)
                            .setScore(score)
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    /**
     * Predicted score = sum(sim(candidate, neighbor) * weight(user, neighbor))
     *                   / sum(sim(candidate, neighbor))
     * where neighbors are K most similar events the user has interacted with.
     */
    private double computePredictedScore(long candidateEventId, Map<Long, Double> userWeightsMap) {
        List<EventSimilarityEntity> neighbors = eventSimilarityRepository
                .findSimilarEventsTopK(candidateEventId, PageRequest.of(0, nearestNeighborsK));

        double weightedSum = 0.0;
        double simSum = 0.0;

        for (EventSimilarityEntity sim : neighbors) {
            long neighborId = sim.getEventA().equals(candidateEventId) ? sim.getEventB() : sim.getEventA();
            Double userWeight = userWeightsMap.get(neighborId);
            if (userWeight != null) {
                weightedSum += sim.getScore() * userWeight;
                simSum += sim.getScore();
            }
        }

        return simSum == 0 ? 0.0 : weightedSum / simSum;
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetSimilarEvents: eventId={}, userId={}, maxResults={}",
                request.getEventId(), request.getUserId(), request.getMaxResults());
        int limit = request.getMaxResults() > 0 ? request.getMaxResults() : defaultMaxResults;

        Set<Long> interactedEvents = userActionRepository.findByUserId(request.getUserId()).stream()
                .map(UserActionEntity::getEventId)
                .collect(Collectors.toSet());

        eventSimilarityRepository.findSimilarEvents(request.getEventId()).stream()
                .filter(sim -> {
                    long other = sim.getEventA().equals(request.getEventId()) ? sim.getEventB() : sim.getEventA();
                    return !interactedEvents.contains(other);
                })
                .sorted(Comparator.comparingDouble(EventSimilarityEntity::getScore).reversed())
                .limit(limit)
                .forEach(sim -> {
                    long other = sim.getEventA().equals(request.getEventId()) ? sim.getEventB() : sim.getEventA();
                    responseObserver.onNext(RecommendedEventProto.newBuilder()
                            .setEventId(other)
                            .setScore(sim.getScore())
                            .build());
                });

        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetInteractionsCount for {} events", request.getEventIdCount());

        List<Long> eventIds = request.getEventIdList();
        List<UserActionEntity> actions = userActionRepository.findByEventIdIn(eventIds);

        // Sum of max weights per user per event
        Map<Long, Double> totals = new HashMap<>();
        for (UserActionEntity action : actions) {
            totals.merge(action.getEventId(), action.getWeight(), Double::sum);
        }

        totals.forEach((eventId, score) ->
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(score)
                        .build()));

        responseObserver.onCompleted();
    }

    @Override
    public void getUserEventMaxWeight(UserEventWeightRequestProto request,
                                      StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetUserEventMaxWeight: userId={}, eventId={}", request.getUserId(), request.getEventId());

        double weight = userActionRepository
                .findByUserIdAndEventId(request.getUserId(), request.getEventId())
                .map(UserActionEntity::getWeight)
                .orElse(0.0);

        responseObserver.onNext(RecommendedEventProto.newBuilder()
                .setEventId(request.getEventId())
                .setScore(weight)
                .build());
        responseObserver.onCompleted();
    }
}
