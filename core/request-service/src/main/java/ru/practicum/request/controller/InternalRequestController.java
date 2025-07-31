package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalRequestController {

    private final RequestService requestService;

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(requestService.getRequestsByEvent(eventId));
    }

    @PostMapping("/requests/batch")
    public ResponseEntity<Void> updateAllRequests(@RequestBody List<ParticipationRequestDto> requests) {
        requestService.updateAll(requests);
        return ResponseEntity.noContent().build();
    }
}
