package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.analyzer.model.Action;

import java.util.List;
import java.util.Set;

public interface ActionRepository extends JpaRepository<Action, Long> {

    // Получить действия пользователя, отсортированные по времени по убыванию
    List<Action> findTopNByUserIdOrderByTimestampDesc(Long userId, int n);

    // Все действия по конкретным событиям
    List<Action> findByEventIdIn(Set<Long> eventIds);

    // ID событий, на которые пользователь взаимодействовал (любые действия)
    @Query("SELECT a.eventId FROM Action a WHERE a.userId = :userId AND a.eventId IN :eventIds")
    Set<Long> findEventIdsByUserIdAndEventIds(Long userId, Set<Long> eventIds);
}

