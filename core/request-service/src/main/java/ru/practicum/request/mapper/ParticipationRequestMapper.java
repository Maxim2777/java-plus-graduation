package ru.practicum.request.mapper;

import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public static ParticipationRequest toEntity(ParticipationRequestDto dto) {
        return ParticipationRequest.builder()
                .id(dto.getId())
                .created(dto.getCreated())
                .eventId(dto.getEvent())                // поле event заменено на eventId
                .requesterId(dto.getRequester())
                .status(dto.getStatus())
                .build();
    }

    public static List<ParticipationRequest> toEntityList(List<ParticipationRequestDto> dtoList) {
        return dtoList.stream()
                .map(ParticipationRequestMapper::toEntity)
                .collect(Collectors.toList());
    }
}
