package ru.practicum.ewm.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.util.List;

@FeignClient(name = "stats-server")
public interface StatClient {

    @PostMapping("/hit")
    void sendHit(@RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam List<String> uris,
            @RequestParam boolean unique
    );
}