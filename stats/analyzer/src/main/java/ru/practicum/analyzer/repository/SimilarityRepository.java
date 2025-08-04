package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.model.SimilarityKey;

import java.util.Collection;
import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, SimilarityKey> {

    List<Similarity> findAllByEventAOrEventB(Long eventA, Long eventB);

    List<Similarity> findAllByEventAInOrEventBIn(Collection<Long> eventAIds, Collection<Long> eventBIds);
}