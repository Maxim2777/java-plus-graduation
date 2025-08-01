package ru.practicum.comment.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.client.InternalEvent;
import ru.practicum.ewm.main.client.UserClient;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.NewCommentDto;
import ru.practicum.ewm.main.dto.UpdateCommentDto;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.params.CommentSearchParamsAdmin;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentService;
import ru.practicum.ewm.main.model.enums.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final InternalEvent internalEventClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto dto) {
        userClient.getUserById(userId); // проверка пользователя

        EventFullDto event;
        try {
            event = internalEventClient.getEventById(dto.getEventId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("The event with id: " + dto.getEventId() + " not found!");
            }
            throw new ValidationException("Can't comment unpublished events or non-public events.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Can't comment unpublished events");
        }

        Comment comment = CommentMapper.toEntity(dto, userId);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteOwnComment(Long userId, Long commentId) {
        // Опционально: проверка, существует ли пользователь
        UserDto userDto = userClient.getUserById(userId);

        // Получение комментария
        Comment comment = getCommentById(commentId);

        // Проверка авторства
        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("User can delete only own comments");
        }

        // Удаление
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        internalEventClient.getEventById(eventId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        return commentRepository.findByEventId(eventId, pageable).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        userClient.getUserById(userId); // Проверка на существование пользователя

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        return commentRepository.findByAuthorId(userId, pageable).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        getCommentById(commentId); // проверка, что комментарий существует
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByAdmin(CommentSearchParamsAdmin params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(),
                Sort.by("createdOn").descending());

        if (params.getAuthorId() != null) {
            userClient.getUserById(params.getAuthorId());
        }

        if (params.getEventId() != null) {
            internalEventClient.getEventById(params.getEventId());
        }

        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (params.getRangeStart() != null) {
            try {
                rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));
            } catch (DateTimeParseException e) {
                throw new ValidationException("Invalid rangeStart format, expected yyyy-MM-dd HH:mm:ss");
            }
        }

        if (params.getRangeEnd() != null) {
            try {
                rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
            } catch (DateTimeParseException e) {
                throw new ValidationException("Invalid rangeEnd format, expected yyyy-MM-dd HH:mm:ss");
            }
        }

        return commentRepository.findByFilters(
                        params.getAuthorId(),
                        params.getEventId(),
                        rangeStart,
                        rangeEnd,
                        pageable
                ).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateOwnComment(Long userId, Long commentId, UpdateCommentDto updateDto) {
        userClient.getUserById(userId); // проверка, что пользователь существует

        Comment comment = getCommentById(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("User can update only their own comment");
        }

        comment.setText(updateDto.getText());
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " not found"));
    }
}