package ru.practicum.ewm.stats.client;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.grpc.UserActionControllerGrpc;
import ru.practicum.ewm.stats.grpc.UserActionProto;

@Component
@Slf4j
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerStub asyncStub;

    public void collectUserAction(UserActionProto action) {
        log.debug("Sending async gRPC call to collector: {}", action.getActionType());

        asyncStub.collectUserAction(action, new StreamObserver<>() {
            @Override
            public void onNext(Empty value) {
                log.debug("Collector acknowledged the user action.");
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC call to collector failed: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.debug("gRPC stream to collector completed.");
            }
        });
    }
}