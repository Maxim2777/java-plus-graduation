package ru.practicum.analyzer.service;

import ru.practicum.recommendation.avro.UserAction;

import java.util.List;

public interface ActionService {
    void save(UserAction action);
    List<Long> findSortedEventIdsOfUser(long userId, int maxResults);
}