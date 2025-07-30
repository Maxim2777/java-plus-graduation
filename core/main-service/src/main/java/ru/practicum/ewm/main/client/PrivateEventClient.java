package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.*;

import java.util.List;

@FeignClient(name = "event-service")
public interface PrivateEventClient {

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                      @RequestParam(value = "from", defaultValue = "0") int from,
                                      @RequestParam(value = "size", defaultValue = "10") int size);

    @PostMapping("/users/{userId}/events")
    EventFullDto createUserEvent(@PathVariable Long userId,
                                 @RequestBody NewEventDto dto);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getUserEventById(@PathVariable Long userId,
                                  @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto updateUserEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @RequestBody UpdateEventUserRequest dto);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getAllParticipationRequestsByUserIdAndEventId(
            @PathVariable Long userId,
            @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                       @PathVariable Long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request);
}