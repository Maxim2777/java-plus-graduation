package ru.practicum.request.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.client.PublicEventClient;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.client.UserClient;
import ru.practicum.ewm.main.model.enums.ParticipationRequestStatus;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.main.model.enums.EventState;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.request.service.RequestService;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final PublicEventClient publicEventClient;
    private final UserClient userClient; // добавил вместо userRepository

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userClient.getUserById(userId); // Проверка, что пользователь существует
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long requesterId, Long eventId) {
        userClient.getUserById(requesterId); // Проверка, что пользователь существует

        EventFullDto event;
        try {
            event = publicEventClient.getEventById(eventId);
        } catch (FeignException.NotFound e) {
            throw new ConflictException("Cannot participate in an unpublished or non-existent event.");
        }

        if (requestRepository.existsByRequesterIdAndEventId(requesterId, eventId)) {
            throw new ConflictException("User already sent a request for this event.");
        }

        if (requesterId.equals(event.getInitiator().getId())) {
            throw new ConflictException("The event initiator cannot submit a participation request for their own event.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Participation in an unpublished event is not allowed.");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

        if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new ConflictException("The event has reached the participation request limit.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(requesterId)
                .eventId(event.getId())
                .created(LocalDateTime.now())
                .status(ParticipationRequestStatus.PENDING)
                .build();

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        }

        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userClient.getUserById(userId); // Проверка, что пользователь существует

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("User can cancel only their own requests.");
        }

        request.setStatus(ParticipationRequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAll(List<ParticipationRequestDto> updatedRequests) {
        List<ParticipationRequest> entities = ParticipationRequestMapper.toEntityList(updatedRequests);
        requestRepository.saveAll(entities);
    }
}