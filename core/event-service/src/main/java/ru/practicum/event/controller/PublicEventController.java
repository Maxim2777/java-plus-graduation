package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.service.EventService;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;
import ru.practicum.ewm.main.grpc.client.CollectorClient;
import ru.practicum.messages.proto.ActionTypeProto;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final CollectorClient collectorClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@ModelAttribute EventParamsPublic params,
                                                         HttpServletRequest request) {
        log.info("PublicEventController - Get public events. params: {}", params);
        return ResponseEntity.ok(eventService.getPublicEvents(params, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long id,
                                                     @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
                                                     HttpServletRequest request) {
        log.info("PublicEventController - Get public event. id: {}", id);

        if (userId != null) {
            collectorClient.sendAction(userId, id, ActionTypeProto.ACTION_VIEW, Instant.now());
        }

        return ResponseEntity.ok(eventService.getEventById(id, request));
    }
}