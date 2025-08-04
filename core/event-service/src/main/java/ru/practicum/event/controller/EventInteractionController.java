package ru.practicum.event.controller;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.messages.proto.RecommendedEventProto;
import ru.practicum.messages.proto.UserActionProto;
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

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    @PutMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id, @RequestParam Long userId) {
        log.info("User {} liked event {}", userId, id);
        Instant now = Instant.now();
        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(id)
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build());
        return ResponseEntity.ok().build();
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
