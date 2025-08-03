package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.recommendation.avro.ActionType;

@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long eventId;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private Long timestamp;
}
