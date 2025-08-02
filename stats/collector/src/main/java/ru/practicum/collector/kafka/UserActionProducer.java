package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.recommendation.avro.UserAction;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProducer {

    private static final String TOPIC = "user.actions";

    private final KafkaTemplate<String, UserAction> kafkaTemplate;

    public void send(UserAction action) {
        log.info("Sending user action to Kafka: {}", action);
        ProducerRecord<String, UserAction> record = new ProducerRecord<>(TOPIC, action);
        kafkaTemplate.send(record);
    }
}
