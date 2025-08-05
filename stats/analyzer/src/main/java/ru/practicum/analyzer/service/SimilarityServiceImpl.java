package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarity;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final SimilarityRepository repository;

    @Override
    public void save(EventSimilarity similarityAvro) {
        Long a = similarityAvro.getEventA();
        Long b = similarityAvro.getEventB();
        if (a.equals(b)) return; // пропустить самих себя

        // Всегда упорядочиваем: eventA < eventB
        long first = Math.min(a, b);
        long second = Math.max(a, b);

        Similarity entity = new Similarity(first, second, similarityAvro.getScore());
        repository.save(entity);
    }
}