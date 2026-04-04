package ru.practicum.client.grpc;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.client.CollectorClient;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
public class CollectorGrpcClient implements CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    @Override
    public void sendUserAction(long userId, long eventId, String actionType) {
        try {
            Instant now = Instant.now();
            UserActionProto action = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(toProtoActionType(actionType))
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            collectorStub.collectUserAction(action);
            log.debug("Sent user action via gRPC: userId={}, eventId={}, type={}", userId, eventId, actionType);
        } catch (Exception e) {
            log.warn("Failed to send user action via gRPC: {}", e.getMessage());
        }
    }

    private ActionTypeProto toProtoActionType(String type) {
        return switch (type) {
            case "REGISTER" -> ActionTypeProto.ACTION_REGISTER;
            case "LIKE" -> ActionTypeProto.ACTION_LIKE;
            default -> ActionTypeProto.ACTION_VIEW;
        };
    }
}
