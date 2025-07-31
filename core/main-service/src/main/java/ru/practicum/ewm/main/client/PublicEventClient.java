package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service", contextId = "PublicEventClient")
public interface PublicEventClient {

    @GetMapping("/events")
    List<EventShortDto> getPublicEvents(@RequestParam(value = "text", required = false) String text,
                                        @RequestParam(value = "categories", required = false) List<Long> categories,
                                        @RequestParam(value = "paid", required = false) Boolean paid,
                                        @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                        @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                        @RequestParam(value = "onlyAvailable", required = false) Boolean onlyAvailable,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @RequestParam(value = "from", defaultValue = "0") int from,
                                        @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/events/{id}")
    EventFullDto getEventById(@PathVariable("id") Long id);
}

