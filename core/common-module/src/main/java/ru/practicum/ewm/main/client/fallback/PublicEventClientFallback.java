package ru.practicum.ewm.main.client.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.client.PublicEventClient;
import ru.practicum.ewm.main.dto.EventFullDto;

@Component
public class PublicEventClientFallback implements PublicEventClient {

    @Override
    public EventFullDto getEventById(Long id) {
        return null;
    }
}