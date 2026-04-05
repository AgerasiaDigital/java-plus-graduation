package ru.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;
    private final org.springframework.core.env.Environment environment;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        UserActionAvro avro = new UserActionAvro();
        avro.setUserId(request.getUserId());
        avro.setEventId(request.getEventId());
        avro.setActionType(mapAction(request.getActionType()));
        com.google.protobuf.Timestamp pts = request.getTimestamp();
        Instant ts = (pts.getSeconds() == 0 && pts.getNanos() == 0)
                ? Instant.now()
                : Instant.ofEpochSecond(pts.getSeconds(), pts.getNanos());
        avro.setTimestamp(ts);
        String topic = environment.getRequiredProperty("ewm.kafka.topic.user-actions");
        kafkaTemplate.send(topic, String.valueOf(request.getUserId()), avro);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static ActionTypeAvro mapAction(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown action");
        };
    }
}
