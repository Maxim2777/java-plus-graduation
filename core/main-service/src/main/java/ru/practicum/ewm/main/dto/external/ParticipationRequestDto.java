package ru.practicum.ewm.main.dto.external;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private String status;
}
