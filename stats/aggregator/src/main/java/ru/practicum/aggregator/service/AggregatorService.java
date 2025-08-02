package ru.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.recommendation.avro.UserAction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AggregatorService {

    private final Map<Long, Integer> viewCounts = new ConcurrentHashMap<>();

    public void aggregate(UserAction action) {
        if ("VIEW".equalsIgnoreCase(action.getActionType().name())) {
            viewCounts.merge(action.getEventId(), 1, Integer::sum);
            log.info("📊 Обновлён счётчик просмотров: eventId={}, count={}",
                    action.getEventId(), viewCounts.get(action.getEventId()));
        }
    }

    public int getViewCount(long eventId) {
        return viewCounts.getOrDefault(eventId, 0);
    }
}