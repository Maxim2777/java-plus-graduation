package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

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

    private double weight; // максимальный вес действия
}