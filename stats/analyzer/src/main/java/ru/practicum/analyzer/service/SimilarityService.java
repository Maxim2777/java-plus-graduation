package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface SimilarityService {
    void save(EventSimilarityAvro similarity);
}