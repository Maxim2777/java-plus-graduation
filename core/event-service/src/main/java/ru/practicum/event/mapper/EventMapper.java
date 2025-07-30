package ru.practicum.event.mapper;

import ru.practicum.event.model.*;
import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.dto.LocationDto;
import ru.practicum.ewm.main.dto.NewEventDto;
import ru.practicum.ewm.main.dto.UserShortDto;
import ru.practicum.ewm.main.model.enums.EventState;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto dto, Long initiatorId) {
        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .category(new Category(dto.getCategory(), null))
                .location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .eventDate(dto.getEventDate())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .paid(dto.isPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.isRequestModeration())
                .initiatorId(initiatorId)
                .build();
    }

    public static EventShortDto toShortDto(Event event, long confirmed, long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .paid(event.isPaid())
                .eventDate(event.getEventDate())
                .initiatorId(event.getInitiatorId())
                // initiatorName добавляется позже
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }

    public static EventFullDto entityToFullDto(Event event, long confirmed, long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .paid(event.isPaid())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .initiatorId(event.getInitiatorId())      // <- только ID
                // initiatorName будет подставлен позже
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDtoFromEvent(Event event, String initiatorName) {
        CategoryDto category = new CategoryDto(event.getCategory().getId(), "Category Name");

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(category)
                .confirmedRequests(0L)
                .eventDate(event.getEventDate())
                .initiator(new UserShortDto(event.getInitiatorId(), initiatorName))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }
}