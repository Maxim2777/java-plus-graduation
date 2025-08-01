package ru.practicum.ewm.main.client.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.client.RequestInternalClient;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;

import java.util.List;

@Component
public class RequestInternalClientFallback implements RequestInternalClient {

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long eventId) {
        return List.of(); // безопасный пустой список
    }

    @Override
    public void updateAll(List<ParticipationRequestDto> requests) {
        // ничего не делаем — метод void
    }
}