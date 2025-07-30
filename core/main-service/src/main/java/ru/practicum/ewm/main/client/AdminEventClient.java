package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.UpdateEventAdminRequest;

import java.util.List;

@FeignClient(name = "event-service")
public interface AdminEventClient {

    @GetMapping("/admin/events")
    List<EventFullDto> getEventsByAdmin(
            @RequestParam(value = "users", required = false) List<Long> users,
            @RequestParam(value = "states", required = false) List<String> states,
            @RequestParam(value = "categories", required = false) List<Long> categories,
            @RequestParam(value = "rangeStart", required = false) String rangeStart,
            @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @PatchMapping("/admin/events/{eventId}")
    EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                    @RequestBody UpdateEventAdminRequest dto);
}
