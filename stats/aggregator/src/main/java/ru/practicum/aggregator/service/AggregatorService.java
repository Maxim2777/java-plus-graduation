package ru.practicum.aggregator.service;

import ru.practicum.recommendation.avro.UserActionAvro;

public interface AggregatorService {
    void handle(UserActionAvro action);
}