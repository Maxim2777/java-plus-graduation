package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.model.ActionKey;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, ActionKey> {
    List<Action> findAllByUserId(Long userId);
    List<Action> findAllByEventId(Long eventId);
}