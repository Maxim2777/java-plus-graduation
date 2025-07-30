package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.params.UserParamsAdmin;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(UserParamsAdmin param);

    void deleteUser(Long userId);

    UserDto getUser(Long id);
}