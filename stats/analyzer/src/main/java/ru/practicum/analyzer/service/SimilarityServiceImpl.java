package ru.practicum.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.analyzer.service.SimilarityService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final SimilarityRepository similarityRepository;

    @Override
    public List<Similarity> findAllContainsEventId(Long eventId) {
        return similarityRepository.findAllByKeyEventIdOrKeyOtherEventId(eventId, eventId);
    }

    @Override
    public List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> userEventIds, int maxResults) {
        List<Similarity> allPairs = new ArrayList<>();
        for (Long eventId : userEventIds) {
            allPairs.addAll(findAllContainsEventId(eventId));
        }

        return allPairs.stream()
                .filter(sim -> {
                    Long e1 = sim.getKey().getEventId();
                    Long e2 = sim.getKey().getOtherEventId();
                    // Исключаем пары, где оба события уже известны пользователю
                    return !(userEventIds.contains(e1) && userEventIds.contains(e2));
                })
                .sorted(Comparator.comparing(Similarity::getScore).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }
}
