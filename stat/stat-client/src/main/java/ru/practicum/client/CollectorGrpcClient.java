package ru.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType) {
        try {
            Instant now = Instant.now();
            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            stub.collectUserAction(request);
            log.debug("Sent user action: userId={}, eventId={}, type={}", userId, eventId, actionType);
        } catch (Exception e) {
            log.warn("Failed to send user action to collector: {}", e.getMessage());
        }
    }
}
