package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CategoryDto;

import java.util.List;

@FeignClient(name = "event-service", contextId = "PublicCategoryClient")
public interface PublicCategoryClient {

    @GetMapping("/categories")
    List<CategoryDto> getAll(@RequestParam(defaultValue = "0") int from,
                             @RequestParam(defaultValue = "10") int size);

    @GetMapping("/categories/{catId}")
    CategoryDto getById(@PathVariable Long catId);
}

