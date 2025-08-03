package ru.practicum.analyzer.service;

import ru.practicum.analyzer.model.Similarity;

import java.util.List;
import java.util.Set;

public interface SimilarityService {

    // Найти все Similarity, где участвует указанный eventId
    List<Similarity> findAllContainsEventId(Long eventId);

    // Найти top-N наиболее релевантных пар, где участвуют события из userEventIds
    List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> userEventIds, int maxResults);
}
