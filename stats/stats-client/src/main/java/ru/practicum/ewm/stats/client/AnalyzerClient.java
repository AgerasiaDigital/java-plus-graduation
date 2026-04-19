package ru.practicum.ewm.stats.client;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.grpc.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.grpc.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.grpc.RecommendedEventProto;
import ru.practicum.ewm.stats.grpc.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.grpc.UserPredictionsRequestProto;

@Component
@Slf4j
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerStub asyncStub;

    public Map<Long, Double> getInteractionsCount(Collection<Long> eventIds) {
        log.debug("Requesting interaction counts for {} events", eventIds.size());
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
            .addAllEventIds(eventIds)
            .build();

        CompletableFuture<List<RecommendedEventProto>> future = new CompletableFuture<>();
        List<RecommendedEventProto> results = new ArrayList<>();

        asyncStub.getInteractionsCount(request, new StreamObserver<>() {
            @Override
            public void onNext(RecommendedEventProto value) {
                results.add(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC stream for GetInteractionsCount failed", t);
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(results);
            }
        });

        try {
            List<RecommendedEventProto> finalResults = future.get(5, TimeUnit.SECONDS);
            return finalResults.stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to get interaction counts from analyzer", e);
            return Map.of();
        }
    }

    public Map<Long, Double> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Requesting recommendations for user {}", userId);
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
            .setUserId(userId)
            .setMaxResults(maxResults)
            .build();

        CompletableFuture<List<RecommendedEventProto>> future = new CompletableFuture<>();
        List<RecommendedEventProto> results = new ArrayList<>();

        asyncStub.getRecommendationsForUser(request, new StreamObserver<RecommendedEventProto>() {
            @Override
            public void onNext(RecommendedEventProto value) {
                results.add(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC stream for GetRecommendationsForUser failed", t);
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(results);
            }
        });

        try {
            List<RecommendedEventProto> finalResults = future.get(5, TimeUnit.SECONDS);
            return finalResults.stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to get recommendations from analyzer", e);
            return Map.of();
        }
    }

    public Map<Long, Double> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        log.debug("Requesting similar events for event {}", eventId);
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
            .setEventId(eventId).setUserId(userId).setMaxResults(maxResults).build();

        CompletableFuture<List<RecommendedEventProto>> future = new CompletableFuture<>();
        List<RecommendedEventProto> results = new ArrayList<>();

        asyncStub.getSimilarEvents(request, new StreamObserver<RecommendedEventProto>() {
            @Override
            public void onNext(RecommendedEventProto value) {
                results.add(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC stream for GetSimilarEvents failed", t);
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(results);
            }
        });

        try {
            List<RecommendedEventProto> finalResults = future.get(5, TimeUnit.SECONDS);
            return finalResults.stream().collect(Collectors.toMap(RecommendedEventProto::getEventId,
                RecommendedEventProto::getScore));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to get similar events from analyzer", e);
            return Map.of();
        }
    }
}