package ru.practicum.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.RecommendationsService;
import ru.practicum.analyzer.service.RecommendationsService.ScoredEvent;
import ru.practicum.grpc.stats.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.dashboard.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.dashboard.RecommendedEventProto;
import ru.practicum.grpc.stats.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.dashboard.UserEventWeightRequestProto;
import ru.practicum.grpc.stats.dashboard.UserPredictionsRequestProto;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        for (ScoredEvent e : recommendationsService.getRecommendationsForUser(
                request.getUserId(), request.getMaxResults())) {
            responseObserver.onNext(toProto(e));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        for (ScoredEvent e : recommendationsService.getSimilarEvents(
                request.getEventId(), request.getUserId(), request.getMaxResults())) {
            responseObserver.onNext(toProto(e));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        for (ScoredEvent e : recommendationsService.getInteractionsCount(request.getEventIdList())) {
            responseObserver.onNext(toProto(e));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getUserEventMaxWeight(
            UserEventWeightRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        double w = recommendationsService.getUserEventMaxWeight(request.getUserId(), request.getEventId());
        responseObserver.onNext(RecommendedEventProto.newBuilder()
                .setEventId(request.getEventId())
                .setScore(w)
                .build());
        responseObserver.onCompleted();
    }

    private static RecommendedEventProto toProto(ScoredEvent e) {
        return RecommendedEventProto.newBuilder()
                .setEventId(e.eventId())
                .setScore(e.score())
                .build();
    }
}
