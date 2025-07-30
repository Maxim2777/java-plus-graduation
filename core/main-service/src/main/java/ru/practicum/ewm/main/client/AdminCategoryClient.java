package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.NewCategoryDto;

@FeignClient(name = "event-service")
public interface AdminCategoryClient {

    @PostMapping("/admin/categories")
    CategoryDto addCategory(@RequestBody NewCategoryDto dto);

    @PatchMapping("/admin/categories/{catId}")
    CategoryDto updateCategory(@PathVariable Long catId,
                               @RequestBody CategoryDto dto);

    @DeleteMapping("/admin/categories/{catId}")
    void deleteCategory(@PathVariable Long catId);
}

