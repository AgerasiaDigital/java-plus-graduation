package ru.practicum.ewm.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.analyzer.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.analyzer.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.analyzer.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.analyzer.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.analyzer.UserPredictionsRequestProto;

import java.util.stream.Stream;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("GetRecommendationsForUser: userId={}, maxResults={}", request.getUserId(), request.getMaxResults());
        try {
            Stream<RecommendedEventProto> recommendations = recommendationService
                    .getRecommendationsForUser(request.getUserId(), request.getMaxResults());
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting recommendations for user {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("GetSimilarEvents: eventId={}, userId={}, maxResults={}",
                request.getEventId(), request.getUserId(), request.getMaxResults());
        try {
            Stream<RecommendedEventProto> results = recommendationService
                    .getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults());
            results.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting similar events for event {}", request.getEventId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("GetInteractionsCount: eventIds={}", request.getEventIdList());
        try {
            Stream<RecommendedEventProto> results = recommendationService
                    .getInteractionsCount(request.getEventIdList());
            results.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting interactions count", e);
            responseObserver.onError(e);
        }
    }
}
