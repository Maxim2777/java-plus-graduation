package ru.practicum.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.ewm.main.client.RequestInternalClient;
import ru.practicum.ewm.main.client.UserClient;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.dto.NewEventDto;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.dto.UpdateEventUserRequest;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.main.dto.params.EventParamsAdmin;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;
import ru.practicum.ewm.main.dto.params.UserParamsAdmin;
import ru.practicum.ewm.main.model.enums.EventState;
import ru.practicum.ewm.main.model.enums.ParticipationRequestStatus;
import ru.practicum.ewm.main.model.enums.RequestStatus;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final UserClient userClient;
    private final RequestInternalClient requestInternalClient;

    // --- PRIVATE API ---

    @Override
    public List<EventShortDto> getUserEvents(Long userId, UserParamsAdmin params) {
        int from = params.getFrom();
        int size = params.getSize();

        PageRequest page = PageRequest.of(from / size, size);
        UserDto userDto = userClient.getUserById(userId);

        BooleanExpression byUserId = QEvent.event.initiatorId.eq(userId);
        List<Event> events = eventRepository.findAll(byUserId, page).getContent();

        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Long> confrmedMap = getConfirmedRequests(events);

        return events
                .stream()
                .map(event -> EventMapper
                        .toShortDto(event, confrmedMap.get(event.getId()), viewsMap.get(event.getId()),
                                userDto.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        UserDto userDto = userClient.getUserById(userId);

        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confrmedMap = getConfirmedRequests(List.of(event));

        return EventMapper.entityToFullDto(event, confrmedMap.get(event.getId()), viewsMap.get(event.getId()),
                userDto.getName());
    }

    @Override
    @Transactional
    public EventFullDto createUserEvent(Long userId, NewEventDto dto) {
        UserDto userDto = userClient.getUserById(userId);

        categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("The category with id: " + dto.getCategory() + " not found!"));

        if (dto.getEventDate().isBefore(LocalDateTime.now()) ||
                !dto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("The date and time of the event cannot be in the past " +
                    "or earlier than two hours from now: " + dto.getEventDate());
        }

        Event event = EventMapper.toEntity(dto, userId);
        Event savedEvent = eventRepository.save(event);

        return EventMapper.entityToFullDto(savedEvent, 0, 0, userDto.getName());
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = getEventById(eventId);

        UserDto userDto = userClient.getUserById(userId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("A published event cannot be modified.");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());

        if (dto.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(dto.getEventDate().replace(" ", "T"));
            if (newEventDate.isBefore(LocalDateTime.now()) ||
                    !newEventDate.isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("The date and time of the event cannot be in the past " +
                        "or earlier than two hours from now: " + dto.getEventDate());
            }
            event.setEventDate(newEventDate);
        }

        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("The category with id: " + dto.getCategory() +
                            " not found!"));
            event.setCategory(category);
        }

        if ("SEND_TO_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(event));

        eventRepository.save(event);

        return EventMapper.entityToFullDto(event, confirmedMap.get(event.getId()), viewsMap.get(event.getId()),
                userDto.getName());
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsByUserIdAndEventId(Long userId, Long eventId) {
        // Проверка существования пользователя
        userClient.getUserById(userId);

        Event event = getEventById(eventId); // локальный вызов

        // Получаем список заявок через Feign-клиент к request-service
        List<ParticipationRequestDto> requests = requestInternalClient.getRequestsByEvent(eventId);

        return requests;
    }


    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest requestUpdate) {
        Event event = getEventById(eventId);
        UserDto userDto = userClient.getUserById(userId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        List<Long> requestIds = requestUpdate.getRequestIds();
        RequestStatus status = requestUpdate.getStatus();
        int requestCount = requestIds.size();
        int limit = event.getParticipantLimit();

        List<ParticipationRequestDto> requests = requestInternalClient.getRequestsByEvent(eventId);

        long currentConfirmed = requests.stream()
                .filter(req -> req.getStatus() == ParticipationRequestStatus.CONFIRMED)
                .count();

        if (currentConfirmed == limit) {
            throw new ConflictException("The request limit for this event has been reached: " + event);
        }

        requests
                .stream()
                .filter(request -> !request.getStatus()
                        .equals(ParticipationRequestStatus.PENDING))
                .forEach(request -> {
                    throw new ConflictException("The status can only be changed for requests that are in the pending " +
                            "state. The request has the status: " + request.getStatus());
                });

        List<ParticipationRequestDto> updatedRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        switch (status) {
            case CONFIRMED: {
                if (limit == 0 || !event.isRequestModeration() || currentConfirmed + requestCount <= limit) {
                    for (ParticipationRequestDto request : requests) {
                        request.setStatus(ParticipationRequestStatus.CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequests.add(request);
                    }
                } else if (currentConfirmed >= limit) {
                    throw new ConflictException("The request limit for this event has been reached: " + event);
                } else {
                    for (ParticipationRequestDto request : requests) {
                        if (limit > currentConfirmed) {
                            request.setStatus(ParticipationRequestStatus.CONFIRMED);
                            updatedRequests.add(request);
                            confirmedRequests.add(request);
                            currentConfirmed = currentConfirmed + 1;
                        } else {
                            request.setStatus(ParticipationRequestStatus.REJECTED);
                            updatedRequests.add(request);
                            rejectedRequests.add(request);
                        }
                    }
                }
                break;
            }
            case REJECTED: {
                for (ParticipationRequestDto request : requests) {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                    updatedRequests.add(request);
                    rejectedRequests.add(request);
                }
            }
            break;
        }

        requestInternalClient.updateAll(updatedRequests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

    // --- PUBLIC API ---

    @Override
    public List<EventShortDto> getPublicEvents(EventParamsPublic params, HttpServletRequest request) {
        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        BooleanBuilder where = new BooleanBuilder();
        QEvent event = QEvent.event;

        String text = params.getText();
        List<Long> categories = params.getCategories();
        boolean onlyAvailable = params.isOnlyAvailable();
        String sort = params.getSort();
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (params.getRangeStart() != null) {
            rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));
        }

        if (params.getRangeEnd() != null) {
            rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
        }

        where.and(event.state.in(EventState.PUBLISHED));

        if (text != null && !text.isEmpty()) {
            where.and(event.annotation.lower().like("%" + text.toLowerCase() + "%")
                    .or(event.description.lower().like("%" + text.toLowerCase() + "%")));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst().equals(0L)) {
                throw new ValidationException("Incorrect list of category IDs: " + categories);
            }
            where.and(event.category.id.in(categories));
        }

        if (params.getPaid() != null) {
            where.and(event.paid.eq(params.getPaid()));
        }

        if (rangeStart != null) {
            where.and(event.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(event.eventDate.before(rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            where.and(event.eventDate.after(LocalDateTime.now()));
        }

        List<Event> events = eventRepository.findAll(where, page).getContent();

        Map<Long, Long> confirmedMap = getConfirmedRequests(events);
        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, String> initiators = buildInitiatorNameMap(events);

        if (onlyAvailable) {
            events = events
                    .stream()
                    .filter(e -> e.getParticipantLimit() > confirmedMap.getOrDefault(e.getId(), 0L))
                    .toList();
        }

        List<EventShortDto> eventShorts = events.stream()
                .map(e -> EventMapper.toShortDto(
                        e,
                        confirmedMap.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L),
                        initiators.getOrDefault(e.getId(), null)
                ))
                .collect(Collectors.toList());

        statClient.sendHit(EndpointHitDto.builder()
                .app("event-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        if (sort == null) {
            return eventShorts;
        }

        return switch (sort) {
            case "VIEWS" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                    .collect(Collectors.toList());
            case "EVENT_DATE" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
            default -> eventShorts;
        };
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not published");
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(event));
        Map<Long, String> initiators = buildInitiatorNameMap(List.of(event));

        statClient.sendHit(EndpointHitDto.builder()
                .app("event-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        return EventMapper.entityToFullDto(event,
                confirmedMap.get(event.getId()), viewsMap.get(event.getId()), initiators.get(event.getId()));
    }

    // --- ADMIN API ---

    @Override
    public List<EventFullDto> getEventsByAdmin(EventParamsAdmin params) {
        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        BooleanBuilder where = new BooleanBuilder();
        QEvent event = QEvent.event;

        List<Long> users = params.getUsers();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (params.getStates() != null && !params.getStates().isEmpty()) {
            List<EventState> states = params.getStates().stream().map(EventState::valueOf).collect(Collectors.toList());
            where.and(event.state.in(states));
        }

        if (users != null && !users.isEmpty()) {
            if (users.size() == 1 && users.getFirst() == 0L) {
                throw new ValidationException("Incorrect list of category IDs: " + categories);
            }
            where.and(event.initiatorId.in(users));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst() == 0L) {
                throw new ValidationException("Incorrect list of category IDs: " + categories);
            }
            where.and(event.category.id.in(categories));
        }

        if (params.getRangeStart() != null) {
            rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));
        }

        if (params.getRangeEnd() != null) {
            rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
        }

        if (rangeStart != null) {
            where.and(event.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(event.eventDate.before(rangeEnd));
        }

        List<Event> events = eventRepository.findAll(where, page).getContent();

        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Long> confirmedMap = getConfirmedRequests(events);
        Map<Long, String> initiators = buildInitiatorNameMap(events);

        return events
                .stream()
                .map(e -> EventMapper.entityToFullDto(e, confirmedMap.get(e.getId()), viewsMap.get(e.getId()),
                        initiators.get(e.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = getEventById(eventId);

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id: " + dto.getCategory() + " not found!"));
            event.setCategory(category);
        }
        if (dto.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(dto.getEventDate().replace(" ", "T"));
            if (newEventDate.isBefore(LocalDateTime.now())) {
                throw new ValidationException("The event date cannot be in the past: " + dto.getEventDate());
            }
            event.setEventDate(newEventDate);
        }

        if (dto.getLocation() != null) {
            event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
        }

        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if ("PUBLISH_EVENT".equals(dto.getStateAction())) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new ConflictException("Event must be in PENDING state to publish");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(dto.getStateAction())) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Cannot reject a published event");
            }
            event.setState(EventState.CANCELED);
        }

        Event savedEvent = eventRepository.save(event);

        Map<Long, Long> viewsMap = getViews(List.of(savedEvent));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(savedEvent));
        Map<Long, String> initiators = buildInitiatorNameMap(List.of(event));

        return EventMapper
                .entityToFullDto(event, confirmedMap.get(savedEvent.getId()), viewsMap.get(savedEvent.getId()),
                        initiators.get(savedEvent.getId()));
    }

    // --- INTERNAL ---

    @Override
    @Transactional
    public EventFullDto getEventByIdInternal(Long eventId) {
        Event event = getEventById(eventId); // найдёт или кинет 404, если не существует

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(event));
        Map<Long, String> initiators = buildInitiatorNameMap(List.of(event));

        return EventMapper.entityToFullDto(
                event,
                confirmedMap.get(event.getId()),
                viewsMap.get(event.getId()),
                initiators.get(event.getId())
        );
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime startDate = events
                .stream()
                .map(Event::getCreatedOn)
                .toList()
                .stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);

        String start = Objects.requireNonNull(startDate).format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);

        List<ViewStatsDto> views = statClient.getStats(start, end, uris, true);

        Map<Long, Long> map = events
                .stream()
                .collect(Collectors.toMap(Event::getId, e -> 0L, (a, b) -> b));

        if (!views.isEmpty()) {
            views.forEach(v -> map.put(Long.parseLong(v.getUri().split("/", 0)[2]),
                    v.getHits()));
        }

        return map;
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> {
                            List<ParticipationRequestDto> allRequests =
                                    requestInternalClient.getRequestsByEvent(event.getId());
                            long confirmedCount = allRequests.stream()
                                    .filter(r -> r.getStatus() == ParticipationRequestStatus.CONFIRMED)
                                    .count();
                            return confirmedCount;
                        }
                ));
    }

    private Event getEventById(Long eventId) {
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        return eventRepository.findOne(byEventId)
                .orElseThrow(() -> new NotFoundException("The event with id: " + eventId + " not found!"));
    }


    private Map<Long, String> buildInitiatorNameMap(List<Event> events) {
        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        List<UserDto> users = userClient.getAll(initiatorIds, 0, 0);

        return users.stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getName));
    }
}