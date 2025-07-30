package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {

    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId);

    @PostMapping("/users/{userId}/requests")
    ParticipationRequestDto addRequest(@PathVariable Long userId,
                                       @RequestParam Long eventId);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                          @PathVariable Long requestId);
}
