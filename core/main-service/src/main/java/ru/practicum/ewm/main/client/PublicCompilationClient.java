package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface PublicCompilationClient {

    @GetMapping("/compilations")
    List<CompilationDto> getCompilations(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                         @RequestParam(value = "from", defaultValue = "0") int from,
                                         @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/compilations/{compId}")
    CompilationDto getCompilationById(@PathVariable("compId") Long compId);
}
