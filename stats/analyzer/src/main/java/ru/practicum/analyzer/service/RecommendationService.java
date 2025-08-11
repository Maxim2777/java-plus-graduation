package ru.practicum.analyzer.service;

import ru.practicum.messages.proto.InteractionsCountRequestProto;
import ru.practicum.messages.proto.RecommendedEventProto;
import ru.practicum.messages.proto.SimilarEventsRequestProto;
import ru.practicum.messages.proto.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEventProto> getRecommendations(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionCounts(InteractionsCountRequestProto request);
}