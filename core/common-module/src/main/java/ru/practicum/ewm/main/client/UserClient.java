package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-service", contextId = "UserClient")
public interface UserClient {

    @PostMapping("/admin/users")
    UserDto createUser(@RequestBody NewUserRequest request);

    @GetMapping("/admin/users")
    List<UserDto> getAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/admin/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    @DeleteMapping("/admin/users/{userId}")
    void deleteUser(@PathVariable Long userId);
}