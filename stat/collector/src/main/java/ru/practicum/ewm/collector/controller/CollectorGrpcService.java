package ru.practicum.ewm.collector.controller;

import com.google.protobuf.Empty;
import java.time.Instant;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    @Value("${ewm.kafka.topic.user-actions}")
    private String userActionsTopic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Received user action: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getActionType());

        try {
            UserActionAvro avro = UserActionAvro.newBuilder()
                    .setUserId(request.getUserId())
                    .setEventId(request.getEventId())
                    .setActionType(toAvro(request.getActionType()))
                    .setTimestamp(Instant.ofEpochMilli(request.getTimestamp().getSeconds() * 1000
                            + request.getTimestamp().getNanos() / 1_000_000))
                    .build();

            kafkaTemplate.send(userActionsTopic, String.valueOf(request.getUserId()), avro)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send user action to Kafka: userId={}, eventId={}, type={}: {}",
                                    request.getUserId(), request.getEventId(), request.getActionType(), ex.getMessage());
                            responseObserver.onError(Status.INTERNAL
                                    .withDescription("Failed to send user action: " + ex.getMessage())
                                    .withCause(ex)
                                    .asRuntimeException());
                        } else {
                            responseObserver.onNext(Empty.getDefaultInstance());
                            responseObserver.onCompleted();
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to process user action: userId={}, eventId={}, type={}: {}",
                    request.getUserId(), request.getEventId(), request.getActionType(), e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to send user action: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private ActionTypeAvro toAvro(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + proto);
        };
    }
}
