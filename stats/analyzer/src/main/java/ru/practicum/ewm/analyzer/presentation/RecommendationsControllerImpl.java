package ru.practicum.ewm.analyzer.presentation;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.analyzer.application.RecommendationsService;
import ru.practicum.ewm.analyzer.domain.Recommendation;
import ru.practicum.ewm.analyzer.infrastructure.mapper.RecommendationMapper;
import ru.practicum.ewm.stats.grpc.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.grpc.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.grpc.RecommendedEventProto;
import ru.practicum.ewm.stats.grpc.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.grpc.UserPredictionsRequestProto;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsControllerImpl extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService recommendationsService;
    private final RecommendationMapper mapper;

    @Override
    public void getSimilarEvents(
        SimilarEventsRequestProto request,
        StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("gRPC request received for GetSimilarEvents: {}", request);

        try {
            List<Recommendation> recommendations = recommendationsService.findSimilarEvents(
                request.getEventId(),
                request.getUserId(),
                request.getMaxResults()
            );

            for (Recommendation recommendation : recommendations) {
                RecommendedEventProto protoMessage = mapper.toProto(recommendation);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();
            log.info("Successfully streamed {} recommendations.", recommendations.size());

        } catch (Exception e) {
            log.error("Error processing GetSimilarEvents request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(
        InteractionsCountRequestProto request,
        StreamObserver<RecommendedEventProto> responseObserver
    ) {
        log.info("gRPC request received for GetInteractionsCount: {}", request);

        try {
            List<Recommendation> recommendations = recommendationsService.getInteractionsCount(
                request.getEventIdsList()
            );

            for (Recommendation recommendation : recommendations) {
                RecommendedEventProto protoMessage = mapper.toProto(recommendation);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();
            log.info("Successfully streamed {} interaction counts.", recommendations.size());

        } catch (Exception e) {
            log.error("Error processing GetInteractionsCount request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void getRecommendationsForUser(
        UserPredictionsRequestProto request,
        StreamObserver<RecommendedEventProto> responseObserver
    ) {
        log.info("gRPC request received for GetRecommendationsForUser: {}", request);
        try {
            List<Recommendation> predictions = recommendationsService.getUserPredictions(
                request.getUserId(),
                request.getMaxResults()
            );

            for (Recommendation prediction : predictions) {
                RecommendedEventProto protoMessage = mapper.toProto(prediction);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();
            log.info("Successfully streamed {} user predictions.", predictions.size());

        } catch (Exception e) {
            log.error("Error processing GetRecommendationsForUser request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }
}
