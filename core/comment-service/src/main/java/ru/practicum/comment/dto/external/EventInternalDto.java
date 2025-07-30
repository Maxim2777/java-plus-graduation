package ru.practicum.comment.dto.external;

import lombok.Data;
import ru.practicum.comment.model.enums.EventState;

@Data
public class EventInternalDto {
    private Long id;
    private EventState state;
}

