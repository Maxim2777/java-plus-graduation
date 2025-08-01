package ru.practicum.ewm.main.client.fallback;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.client.UserClient;
import ru.practicum.ewm.main.dto.UserDto;

import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        return List.of(); // безопаснее, чем null
    }

    @Override
    public UserDto getUserById(Long userId) {
        return null;
    }
}