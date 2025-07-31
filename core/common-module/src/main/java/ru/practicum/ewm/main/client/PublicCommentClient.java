package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CommentDto;

import java.util.List;

@FeignClient(name = "comment-service", contextId = "PublicCommentClient")
public interface PublicCommentClient {

    @GetMapping("/events/{eventId}/comments")
    List<CommentDto> getComments(@PathVariable Long eventId,
                                 @RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size);
}
