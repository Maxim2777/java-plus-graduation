package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "similarities", uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "otherEventId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Similarity {

    @EmbeddedId
    private SimilarityKey key;

    private double score;
}
