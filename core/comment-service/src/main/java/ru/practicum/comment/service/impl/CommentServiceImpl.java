package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.NewCommentDto;
import ru.practicum.ewm.main.dto.UpdateCommentDto;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.params.CommentSearchParamsAdmin;
import ru.practicum.comment.exception.ConflictException;
import ru.practicum.comment.exception.NotFoundException;
import ru.practicum.comment.exception.ValidationException;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentService;
import ru.practicum.ewm.main.client.PublicEventClient;
import ru.practicum.ewm.main.client.UserClient;
import ru.practicum.ewm.main.model.enums.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PublicEventClient publicEventClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto dto) {
        UserDto userDto = userClient.getUserById(userId); // проверка пользователя

        // проверка события — через Feign-клиент main-service
        EventFullDto event = publicEventClient.getEventById(dto.getEventId());
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Can't comment unpublished events");
        }

        // создаём Comment без сущности Event
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
        publicEventClient.getEventById(eventId); // Проверка на существование события

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

        // Проверка: существует ли автор (если указан)
        if (params.getAuthorId() != null) {
            userClient.getUserById(params.getAuthorId());
        }

        // Проверка: существует ли событие (если указано)
        if (params.getEventId() != null) {
            publicEventClient.getEventById(params.getEventId());
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