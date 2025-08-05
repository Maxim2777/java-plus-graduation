package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.ActionService;
import ru.practicum.recommendation.avro.UserActionAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final ActionService actionService;

    @KafkaListener(topics = "stats.user-actions.v1", groupId = "analyzer", containerFactory = "userActionKafkaListener")
    public void consume(UserActionAvro action) {
        log.info("Analyzer received UserAction: {}", action);
        actionService.save(action);
    }
}