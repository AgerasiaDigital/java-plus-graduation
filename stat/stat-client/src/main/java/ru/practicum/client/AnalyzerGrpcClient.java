package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.dashboard.RecommendedEventProto;
import ru.practicum.grpc.stats.dashboard.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.dashboard.UserEventWeightRequestProto;
import ru.practicum.grpc.stats.dashboard.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            return asStream(stub.getRecommendationsForUser(request));
        } catch (Exception e) {
            log.warn("Failed to get recommendations for user {}: {}", userId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            return asStream(stub.getSimilarEvents(request));
        } catch (Exception e) {
            log.warn("Failed to get similar events for event {}: {}", eventId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            return asStream(stub.getInteractionsCount(request));
        } catch (Exception e) {
            log.warn("Failed to get interactions count: {}", e.getMessage());
            return Stream.empty();
        }
    }

    public RecommendedEventProto getUserEventMaxWeight(long userId, long eventId) {
        try {
            UserEventWeightRequestProto request = UserEventWeightRequestProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .build();
            return stub.getUserEventMaxWeight(request);
        } catch (Exception e) {
            log.warn("Failed to get user event weight: {}", e.getMessage());
            return RecommendedEventProto.newBuilder().setEventId(eventId).setScore(0).build();
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
