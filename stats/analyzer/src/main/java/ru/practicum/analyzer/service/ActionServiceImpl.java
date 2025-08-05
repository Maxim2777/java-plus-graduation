package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.model.ActionKey;
import ru.practicum.analyzer.repository.ActionRepository;
import ru.practicum.recommendation.avro.ActionTypeAvro;
import ru.practicum.recommendation.avro.UserActionAvro;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;

    private final Map<ActionTypeAvro, Integer> actionWeights = Map.of(
            ActionTypeAvro.VIEW, 1,
            ActionTypeAvro.REGISTER, 2,
            ActionTypeAvro.LIKE, 3
    );

    @Override
    public void save(UserActionAvro userAction) {
        int newWeight = actionWeights.getOrDefault(userAction.getActionType(), 0);

        Optional<Action> existing = actionRepository.findById(
                new ActionKey(userAction.getUserId(), userAction.getEventId()));

        if (existing.isEmpty() || newWeight > existing.get().getWeight()) {
            Action updated = new Action();
            updated.setUserId(userAction.getUserId());
            updated.setEventId(userAction.getEventId());
            updated.setWeight(newWeight);
            actionRepository.save(updated);
        }
    }

    @Override
    public List<Long> findSortedEventIdsOfUser(long userId, int maxResults) {
        return actionRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparingInt(Action::getWeight).reversed())
                .limit(maxResults)
                .map(Action::getEventId)
                .collect(Collectors.toList());
    }
}

