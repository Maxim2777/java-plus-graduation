package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    private static final String TOPIC = "stats.events-similarity.v1";

    public void send(EventSimilarityAvro similarity) {
        log.info("Publishing similarity: {}", similarity);
        kafkaTemplate.send(TOPIC, similarity);
    }
}