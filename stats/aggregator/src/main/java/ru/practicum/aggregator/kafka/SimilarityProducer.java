package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.recommendation.avro.EventSimilarity;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityProducer {

    private final KafkaTemplate<String, EventSimilarity> kafkaTemplate;
    private static final String TOPIC = "stats.events-similarity.v1";

    public void send(EventSimilarity similarity) {
        log.info("Publishing similarity: {}", similarity);
        kafkaTemplate.send(TOPIC, similarity);
    }
}