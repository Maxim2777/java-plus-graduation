package ru.practicum.ewm.main.client.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.client.InternalEvent;
import ru.practicum.ewm.main.dto.EventFullDto;

@Component
public class InternalEventFallback implements InternalEvent {

    @Override
    public EventFullDto getEventById(Long id) {
        return null; // Возвращаем null, если event-service недоступен
    }
}
