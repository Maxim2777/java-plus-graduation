package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.recommendation.avro.UserAction;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<String, UserAction> kafkaTemplate;
    private static final String TOPIC = "stats.user-actions.v1";

    public void send(UserAction action) {
        log.info("Sending action to Kafka: {}", action);
        kafkaTemplate.send(TOPIC, String.valueOf(action.getUserId()), action);
    }
}