package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.recommendation.avro.ActionType;

@Entity
@IdClass(ActionKey.class)
@Table(name = "actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    private Long userId;

    @Id
    private Long eventId;

    private int weight; // максимальный вес действия
}