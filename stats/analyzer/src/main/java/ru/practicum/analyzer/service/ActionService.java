package ru.practicum.analyzer.service;

import ru.practicum.analyzer.model.Action;

import java.util.List;
import java.util.Set;

public interface ActionService {

    // Список ID событий, на которые пользователь реагировал (по убыванию времени, максимум maxResults)
    List<Long> findSortedEventIdsOfUser(Long userId, int maxResults);

    // Все действия по списку eventId (например, для подсчёта метрик)
    List<Action> findActionsByEventIds(Set<Long> eventIds);

    // Список ID событий, на которые пользователь уже реагировал (чтобы исключить)
    Set<Long> findEventIds(Long userId, Set<Long> eventIds);
}
