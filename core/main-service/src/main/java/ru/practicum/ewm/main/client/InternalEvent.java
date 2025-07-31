package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.main.dto.EventFullDto;

@FeignClient(name = "event-service", contextId = "internalEventClient")
public interface InternalEvent {

    @GetMapping("/internal/events/{id}")
    EventFullDto getEventById(@PathVariable Long id);
}

