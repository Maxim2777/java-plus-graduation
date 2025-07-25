package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.NewCommentDto;
import ru.practicum.ewm.main.model.Comment;
import ru.practicum.ewm.main.model.Event;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toEntity(NewCommentDto dto, Long authorId, Event event) {
        return Comment.builder()
                .authorId(authorId)
                .event(event)
                .text(dto.getText())
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEvent().getId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .build();
    }
}