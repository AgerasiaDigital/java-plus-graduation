package ru.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.collector.kafka.UserActionKafkaProducer;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionKafkaProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Received user action: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getActionType());

        long timestampMs = request.hasTimestamp()
                ? request.getTimestamp().getSeconds() * 1000L + request.getTimestamp().getNanos() / 1_000_000
                : Instant.now().toEpochMilli();

        UserActionAvro avro = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(toAvroActionType(request.getActionType()))
                .setTimestamp(timestampMs)
                .build();

        producer.send(avro);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private ActionTypeAvro toAvroActionType(ActionTypeProto type) {
        return switch (type) {
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> ActionTypeAvro.VIEW;
        };
    }
}
