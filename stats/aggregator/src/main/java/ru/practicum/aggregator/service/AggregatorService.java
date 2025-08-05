package ru.practicum.aggregator.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface AggregatorService {
    void handle(UserActionAvro action);
}