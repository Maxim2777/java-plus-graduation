package ru.practicum.analyzer.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityKey implements Serializable {
    private Long eventA;
    private Long eventB;
}