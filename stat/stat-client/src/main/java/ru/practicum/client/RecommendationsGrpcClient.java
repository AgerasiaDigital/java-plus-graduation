package ru.practicum.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import ru.practicum.grpc.stats.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.dashboard.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.dashboard.RecommendedEventProto;
import ru.practicum.grpc.stats.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.dashboard.UserEventWeightRequestProto;
import ru.practicum.grpc.stats.dashboard.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class RecommendationsGrpcClient {

    private final DiscoveryClient discoveryClient;
    private final String serviceId;

    private volatile ManagedChannel channel;
    private volatile RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;
    private volatile String cachedTarget;

    public List<EventScore> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto req = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            List<EventScore> out = new ArrayList<>();
            stub().getRecommendationsForUser(req).forEachRemaining(r -> out.add(toScore(r)));
            return out;
        } catch (Exception e) {
            log.warn("Analyzer getRecommendationsForUser failed: {}", e.getMessage());
            resetChannel();
            throw e;
        }
    }

    public List<EventScore> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto req = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            List<EventScore> out = new ArrayList<>();
            stub().getSimilarEvents(req).forEachRemaining(r -> out.add(toScore(r)));
            return out;
        } catch (Exception e) {
            log.warn("Analyzer getSimilarEvents failed: {}", e.getMessage());
            resetChannel();
            throw e;
        }
    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        try {
            InteractionsCountRequestProto req = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            Map<Long, Double> map = new LinkedHashMap<>();
            stub().getInteractionsCount(req).forEachRemaining(r -> map.put(r.getEventId(), r.getScore()));
            return map;
        } catch (Exception e) {
            log.warn("Analyzer getInteractionsCount failed: {}", e.getMessage());
            resetChannel();
            return Map.of();
        }
    }

    public double getUserEventMaxWeight(long userId, long eventId) {
        try {
            UserEventWeightRequestProto req = UserEventWeightRequestProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .build();
            RecommendedEventProto r = stub().getUserEventMaxWeight(req);
            return r.getScore();
        } catch (Exception e) {
            log.warn("Analyzer getUserEventMaxWeight failed: {}", e.getMessage());
            resetChannel();
            return 0.0;
        }
    }

    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub() {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            throw new IllegalStateException("No Eureka instances for service: " + serviceId);
        }
        ServiceInstance i = instances.getFirst();
        int port = GrpcInstanceMetadata.parseGrpcPort(i, 9102);
        String target = i.getHost() + ":" + port;
        if (stub == null || !target.equals(cachedTarget)) {
            shutdownQuietly();
            cachedTarget = target;
            channel = ManagedChannelBuilder.forAddress(i.getHost(), port)
                    .usePlaintext()
                    .build();
            stub = RecommendationsControllerGrpc.newBlockingStub(channel);
        }
        return stub;
    }

    private static EventScore toScore(RecommendedEventProto r) {
        return new EventScore(r.getEventId(), r.getScore());
    }

    private void resetChannel() {
        shutdownQuietly();
        cachedTarget = null;
        stub = null;
    }

    private void shutdownQuietly() {
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            channel = null;
        }
    }

    public void shutdown() {
        shutdownQuietly();
    }

    public record EventScore(long eventId, double score) {
    }
}
