package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.ActionRepository;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.messages.proto.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ActionRepository actionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public List<RecommendedEventProto> getRecommendations(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        // 1. Получить действия пользователя и отсортировать по убыванию веса
        List<Action> actions = actionRepository.findAllByUserId(userId);
        if (actions.isEmpty()) return Collections.emptyList();

        // Карта <eventId, weight> — только последние N (весомых) событий
        Map<Long, Integer> userEventWeights = actions.stream()
                .sorted(Comparator.comparingInt(Action::getWeight).reversed())
                .limit(20) // пусть будет фиксированный буфер последних N
                .collect(Collectors.toMap(
                        Action::getEventId,
                        Action::getWeight,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        Set<Long> knownEvents = userEventWeights.keySet();

        // 2. Получить все схожести, где хотя бы одно из мероприятий — известное пользователю
        List<Similarity> similarities = similarityRepository.findAllByEventAInOrEventBIn(knownEvents, knownEvents);

        // 3. Оценка новых мероприятий: карта <eventId, PredictedScore>
        Map<Long, Double> numerator = new HashMap<>();
        Map<Long, Double> denominator = new HashMap<>();

        for (Similarity sim : similarities) {
            Long a = sim.getEventA();
            Long b = sim.getEventB();
            Double score = sim.getScore();

            // найти: какой из пары — известный, какой — новый
            if (knownEvents.contains(a) && !knownEvents.contains(b)) {
                int weight = userEventWeights.get(a);
                numerator.merge(b, score * weight, Double::sum);
                denominator.merge(b, score, Double::sum);
            } else if (knownEvents.contains(b) && !knownEvents.contains(a)) {
                int weight = userEventWeights.get(b);
                numerator.merge(a, score * weight, Double::sum);
                denominator.merge(a, score, Double::sum);
            }
        }

        // 4. Собираем рекомендации
        return numerator.entrySet().stream()
                .filter(e -> denominator.getOrDefault(e.getKey(), 0.0) > 0)
                .map(e -> {
                    long eventId = e.getKey();
                    double predicted = e.getValue() / denominator.get(eventId);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(predicted)
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long targetEventId = request.getEventId();

        // Получаем все Similarity, где eventA == targetEventId или eventB == targetEventId
        List<Similarity> similarities = similarityRepository.findAllByEventAOrEventB(targetEventId, targetEventId);

        // Получаем события, с которыми уже взаимодействовал пользователь
        Set<Long> interacted = actionRepository.findAllByUserId(request.getUserId()).stream()
                .map(Action::getEventId)
                .collect(Collectors.toSet());

        return similarities.stream()
                .map(similarity -> {
                    Long other = similarity.getEventA().equals(targetEventId)
                            ? similarity.getEventB()
                            : similarity.getEventA();
                    return Map.entry(other, similarity.getScore());
                })
                .filter(entry -> !interacted.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedEventProto> getInteractionCounts(InteractionsCountRequestProto request) {
        Map<Long, Double> eventScores = new HashMap<>();
        for (Long eventId : request.getEventIdList()) {
            double total = actionRepository.findAllByEventId(eventId).stream()
                    .mapToDouble(Action::getWeight)
                    .sum();
            eventScores.put(eventId, total);
        }

        return eventScores.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}