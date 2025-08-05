package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.kafka.SimilarityProducer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarity;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final SimilarityProducer similarityProducer;

    private final Map<Long, Map<Long, Integer>> weights = new HashMap<>();
    private final Map<Long, Double> normSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> similarityMatrix = new HashMap<>();

    private final Map<ActionTypeAvro, Integer> actionWeights = Map.of(
            ActionTypeAvro.VIEW, 1,
            ActionTypeAvro.REGISTER, 2,
            ActionTypeAvro.LIKE, 3
    );

    @Override
    public void handle(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        int weight = actionWeights.getOrDefault(action.getActionType(), 0);

        weights.computeIfAbsent(eventId, e -> new HashMap<>());
        Map<Long, Integer> userMap = weights.get(eventId);
        Integer current = userMap.getOrDefault(userId, 0);

        if (weight <= current) return;

        userMap.put(userId, weight);
        recomputeSimilarity(eventId, userId, weight, action.getTimestamp());
    }

    private void recomputeSimilarity(long eventId, long userId, int updatedWeight, Instant timestamp) {
        normSums.put(eventId, weights.get(eventId).values().stream()
                .mapToDouble(w -> w * w).sum());

        for (Map.Entry<Long, Map<Long, Integer>> entry : weights.entrySet()) {
            long otherEvent = entry.getKey();
            if (eventId == otherEvent) continue;

            Map<Long, Integer> otherUsers = entry.getValue();
            if (!otherUsers.containsKey(userId)) continue;

            int weightA = updatedWeight;
            int weightB = otherUsers.get(userId);

            long a = Math.min(eventId, otherEvent);
            long b = Math.max(eventId, otherEvent);

            similarityMatrix
                    .computeIfAbsent(a, e -> new HashMap<>())
                    .merge(b, Math.min(weightA, weightB) * 1.0, Double::sum);

            double numerator = similarityMatrix.get(a).get(b);
            double denom = Math.sqrt(normSums.get(eventId) * normSums.get(otherEvent));
            double score = denom == 0 ? 0 : numerator / denom;

            EventSimilarity similarity = EventSimilarity.newBuilder()
                    .setEventA(a)
                    .setEventB(b)
                    .setScore(score)
                    .setTimestamp(timestamp)
                    .build();

            similarityProducer.send(similarity);
        }
    }
}