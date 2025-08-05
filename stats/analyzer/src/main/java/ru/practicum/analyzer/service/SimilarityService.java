package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarity;

public interface SimilarityService {
    void save(EventSimilarity similarity);
}