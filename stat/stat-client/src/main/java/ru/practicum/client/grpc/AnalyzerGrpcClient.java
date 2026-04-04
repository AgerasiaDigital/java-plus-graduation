package ru.practicum.client.grpc;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.grpc.stats.analyzer.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.analyzer.RecommendedEventProto;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.analyzer.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.analyzer.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AnalyzerGrpcClient implements AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    @Override
    public List<Long> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> it = analyzerStub.getRecommendationsForUser(request);
            List<Long> result = new ArrayList<>();
            it.forEachRemaining(r -> result.add(r.getEventId()));
            return result;
        } catch (Exception e) {
            log.warn("Failed to get recommendations for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Long> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> it = analyzerStub.getSimilarEvents(request);
            List<Long> result = new ArrayList<>();
            it.forEachRemaining(r -> result.add(r.getEventId()));
            return result;
        } catch (Exception e) {
            log.warn("Failed to get similar events for eventId {}: {}", eventId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            Iterator<RecommendedEventProto> it = analyzerStub.getInteractionsCount(request);
            Map<Long, Double> result = new HashMap<>();
            it.forEachRemaining(r -> result.put(r.getEventId(), (double) r.getScore()));
            return result;
        } catch (Exception e) {
            log.warn("Failed to get interactions count: {}", e.getMessage());
            return Map.of();
        }
    }
}
