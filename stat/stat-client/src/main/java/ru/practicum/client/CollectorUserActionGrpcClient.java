package ru.practicum.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class CollectorUserActionGrpcClient {

    private final DiscoveryClient discoveryClient;
    private final String serviceId;

    private volatile ManagedChannel channel;
    private volatile UserActionControllerGrpc.UserActionControllerBlockingStub stub;
    private volatile String cachedTarget;

    public void collectView(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void collectRegister(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void collectLike(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void send(long userId, long eventId, ActionTypeProto actionType) {
        try {
            Instant now = Instant.now();
            UserActionProto req = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            stub().collectUserAction(req);
        } catch (Exception e) {
            log.warn("Collector gRPC failed: {}", e.getMessage());
            resetChannel();
            throw e;
        }
    }

    private UserActionControllerGrpc.UserActionControllerBlockingStub stub() {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            throw new IllegalStateException("No Eureka instances for service: " + serviceId);
        }
        ServiceInstance i = instances.getFirst();
        int port = GrpcInstanceMetadata.parseGrpcPort(i, 9101);
        String target = i.getHost() + ":" + port;
        if (stub == null || !target.equals(cachedTarget)) {
            shutdownQuietly();
            cachedTarget = target;
            channel = ManagedChannelBuilder.forAddress(i.getHost(), port)
                    .usePlaintext()
                    .build();
            stub = UserActionControllerGrpc.newBlockingStub(channel);
        }
        return stub;
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
}
