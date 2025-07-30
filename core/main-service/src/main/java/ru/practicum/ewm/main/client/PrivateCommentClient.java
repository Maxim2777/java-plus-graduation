package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.NewCommentDto;
import ru.practicum.ewm.main.dto.UpdateCommentDto;

import java.util.List;

@FeignClient(name = "comment-service")
public interface PrivateCommentClient {

    @PostMapping("/users/{userId}/comments")
    CommentDto createComment(@PathVariable Long userId,
                             @RequestBody NewCommentDto dto);

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    void deleteOwnComment(@PathVariable Long userId,
                          @PathVariable Long commentId);

    @GetMapping("/users/{userId}/comments")
    List<CommentDto> getUserComments(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "10") int size);

    @PatchMapping("/users/{userId}/comments/{commentId}")
    CommentDto updateOwnComment(@PathVariable Long userId,
                                @PathVariable Long commentId,
                                @RequestBody UpdateCommentDto updateDto);
}