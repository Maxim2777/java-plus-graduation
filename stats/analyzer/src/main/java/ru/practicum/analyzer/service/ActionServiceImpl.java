package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.repository.ActionRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;

    @Override
    public List<Long> findSortedEventIdsOfUser(Long userId, int maxResults) {
        return actionRepository.findTopNByUserIdOrderByTimestampDesc(userId, maxResults)
                .stream()
                .map(Action::getEventId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Action> findActionsByEventIds(Set<Long> eventIds) {
        return actionRepository.findByEventIdIn(eventIds);
    }

    @Override
    public Set<Long> findEventIds(Long userId, Set<Long> eventIds) {
        return actionRepository.findEventIdsByUserIdAndEventIds(userId, eventIds);
    }
}
