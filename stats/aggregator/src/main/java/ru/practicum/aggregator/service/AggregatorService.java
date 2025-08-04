package ru.practicum.aggregator.service;

import ru.practicum.recommendation.avro.UserAction;

public interface AggregatorService {
    void handle(UserAction action);
}