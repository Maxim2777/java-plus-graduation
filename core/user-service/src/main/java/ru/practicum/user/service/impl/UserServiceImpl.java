package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.exception.ConflictException;
import ru.practicum.user.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with this email already exists");
        }
        User user = UserMapper.toEntity(request);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids);
        } else {
            users = userRepository.findAll(PageRequest.of(from / size, size)).getContent();
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user with id: " + userId + " not found!"));
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return UserMapper.toDto(user);
    }
}