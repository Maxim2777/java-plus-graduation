package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CommentDto;

import java.util.List;

@FeignClient(name = "comment-service", contextId = "AdminCommentClient")
public interface AdminCommentClient {

    @DeleteMapping("/admin/comments/{commentId}")
    void deleteComment(@PathVariable("commentId") Long commentId);

    @GetMapping("/admin/comments")
    List<CommentDto> getCommentsByAdmin(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "eventId", required = false) Long eventId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "rangeStart", required = false) String rangeStart,
            @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}

