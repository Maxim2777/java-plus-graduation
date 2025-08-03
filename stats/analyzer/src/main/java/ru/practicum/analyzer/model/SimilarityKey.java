package ru.practicum.analyzer.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SimilarityKey implements Serializable {

    private Long eventId;

    private Long otherEventId;
}
