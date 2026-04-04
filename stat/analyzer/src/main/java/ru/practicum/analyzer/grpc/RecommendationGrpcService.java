package ru.practicum.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.grpc.stats.analyzer.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.analyzer.RecommendedEventProto;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.analyzer.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.analyzer.UserPredictionsRequestProto;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetRecommendationsForUser: userId={}, maxResults={}", request.getUserId(), request.getMaxResults());
        int maxResults = request.getMaxResults() > 0 ? request.getMaxResults() : 10;
        List<long[]> recommendations = analyzerService.getRecommendationsForUser(request.getUserId(), maxResults);
        sendStreamedResults(recommendations, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetSimilarEvents: eventId={}, userId={}", request.getEventId(), request.getUserId());
        List<long[]> similar = analyzerService.getSimilarEvents(
                request.getEventId(), request.getUserId(), request.getMaxResults());
        sendStreamedResults(similar, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetInteractionsCount: eventIds count={}", request.getEventIdCount());
        List<long[]> counts = analyzerService.getInteractionsCount(request.getEventIdList());
        sendStreamedResults(counts, responseObserver);
    }

    private void sendStreamedResults(List<long[]> data, StreamObserver<RecommendedEventProto> responseObserver) {
        for (long[] pair : data) {
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(pair[0])
                    .setScore((float) Double.longBitsToDouble(pair[1]))
                    .build());
        }
        responseObserver.onCompleted();
    }
}
