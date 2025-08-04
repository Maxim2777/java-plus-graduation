package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@IdClass(SimilarityKey.class)
@Table(name = "similarities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Similarity {

    @Id
    @Column(name = "event_a")
    private Long eventA;

    @Id
    @Column(name = "event_b")
    private Long eventB;

    private double score;
}