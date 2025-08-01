package ru.practicum.user.service;

import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.params.UserParamsAdmin;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(UserParamsAdmin param);

    void deleteUser(Long userId);

    UserDto getUser(Long id);
}