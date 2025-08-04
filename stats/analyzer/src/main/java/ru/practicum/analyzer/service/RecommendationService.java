package ru.practicum.analyzer.service;

import ru.practicum.messages.proto.*;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEventProto> getRecommendations(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionCounts(InteractionsCountRequestProto request);
}