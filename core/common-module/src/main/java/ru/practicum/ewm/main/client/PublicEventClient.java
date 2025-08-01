package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.EventFullDto;

@FeignClient(
        name = "event-service",
        contextId = "PublicEventClient",
        fallback = ru.practicum.ewm.main.client.fallback.PublicEventClientFallback.class
)
public interface PublicEventClient {

    @GetMapping("/events/{id}")
    EventFullDto getEventById(@PathVariable("id") Long id);
}