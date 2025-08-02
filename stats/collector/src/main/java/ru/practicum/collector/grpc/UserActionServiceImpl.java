package ru.practicum.collector.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.kafka.UserActionProducer;
import ru.practicum.recommendation.proto.UserActionControllerGrpc;
import ru.practicum.recommendation.proto.UserActionRequest;
import ru.practicum.recommendation.proto.UserActionResponse;
import ru.practicum.recommendation.proto.ActionType;

import ru.practicum.recommendation.avro.UserAction;        // Avro generated
import ru.practicum.recommendation.avro.ActionType as AvroActionType; // Avro enum

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionServiceImpl extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionProducer producer;

    @Override
    public void send(UserActionRequest request, StreamObserver<UserActionResponse> responseObserver) {
        log.info("Received gRPC UserActionRequest: {}", request);

        // ✅ Преобразуем gRPC enum → Avro enum
        AvroActionType avroActionType = switch (request.getActionType()) {
            case ACTION_VIEW -> AvroActionType.ACTION_VIEW;
            case ACTION_REGISTER -> AvroActionType.ACTION_REGISTER;
            case ACTION_LIKE -> AvroActionType.ACTION_LIKE;
            default -> AvroActionType.ACTION_UNKNOWN;
        };

        // ✅ Сборка Avro-модели
        UserAction action = UserAction.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(avroActionType)
                .setTimestamp(Instant.ofEpochSecond(
                        request.getTimestamp().getSeconds(),
                        request.getTimestamp().getNanos()
                ))
                .build();

        // ✅ Отправка в Kafka
        producer.send(action);

        // ✅ Ответ клиенту
        UserActionResponse response = UserActionResponse.newBuilder()
                .setStatus("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}