package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import java.util.List;

public interface ActionService {
    void save(UserActionAvro action);
    List<Long> findSortedEventIdsOfUser(long userId, int maxResults);
}