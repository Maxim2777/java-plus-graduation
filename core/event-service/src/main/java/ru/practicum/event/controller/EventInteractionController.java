package ru.practicum.event.controller;

import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.grpc.client.CollectorClient;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.messages.proto.RecommendedEventProto;
import ru.practicum.messages.proto.ActionTypeProto;
import ru.practicum.messages.proto.UserPredictionsRequestProto;


import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventInteractionController {

    private final CollectorClient collectorClient;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    @PutMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id,
                                     @RequestParam Long userId) {
        log.info("User {} liked event {}", userId, id);

        try {
            collectorClient.sendAction(userId, id, ActionTypeProto.ACTION_LIKE, Instant.now());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to send like action to Collector via gRPC", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(@RequestParam Long userId,
                                                @RequestParam(required = false, defaultValue = "10") int maxResults) {
        log.info("Getting recommendations for user {}", userId);
        var request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> recommendations = analyzerClient.getRecommendationsForUser(request);

        List<RecommendedEventProto> result = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(recommendations, 0), false)
                .toList();

        return ResponseEntity.ok(result);
    }
}
