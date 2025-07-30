package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequest;

@FeignClient(name = "event-service")
public interface AdminCompilationClient {

    @PostMapping("/admin/compilations")
    CompilationDto create(@RequestBody NewCompilationDto newCompilationDto);

    @DeleteMapping("/admin/compilations/{compId}")
    void deleteById(@PathVariable("compId") Long compId);

    @PatchMapping("/admin/compilations/{compId}")
    CompilationDto update(@PathVariable("compId") Long compId,
                          @RequestBody UpdateCompilationRequest updateCompilationRequest);
}

