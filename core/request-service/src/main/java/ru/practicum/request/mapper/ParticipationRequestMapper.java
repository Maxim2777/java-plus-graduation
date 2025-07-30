package ru.practicum.request.mapper;

import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

public class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())        // ✅ заменено
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }
}