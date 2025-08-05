package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarity;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final SimilarityService similarityService;

    @KafkaListener(topics = "stats.events-similarity.v1", groupId = "analyzer", containerFactory = "similarityKafkaListener")
    public void consume(EventSimilarity similarity) {
        log.info("Analyzer received EventSimilarity: {}", similarity);
        similarityService.save(similarity);
    }
}