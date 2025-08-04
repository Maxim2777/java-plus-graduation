package ru.practicum.analyzer.service;

import ru.practicum.recommendation.avro.EventSimilarity;

public interface SimilarityService {
    void save(EventSimilarity similarity);
}