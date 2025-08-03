package ru.practicum.analyzer.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.service.ActionService;
import ru.practicum.analyzer.service.SimilarityService;
import ru.practicum.recommendation.proto.*;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationServiceImpl extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendations(UserPredictionsRequestProto request,
                                   StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("📡 gRPC: getRecommendations для userId={}", request.getUserId());

        List<RecommendedEventProto> recommendations = analyzerService.getRecommendations(request);
        recommendations.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("📡 gRPC: getSimilarEvents для eventId={}, userId={}", request.getEventId(), request.getUserId());

        List<RecommendedEventProto> similarEvents = analyzerService.getSimilarEvents(request);
        similarEvents.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("📡 gRPC: getInteractionsCount по eventIdList={}", request.getEventIdList());

        List<RecommendedEventProto> interactions = analyzerService.getInteractionsCount(request);
        interactions.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }
}
