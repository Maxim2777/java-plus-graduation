package ru.practicum.ewm.main.grpc.client;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.messages.proto.ActionTypeProto;
import ru.practicum.messages.proto.UserActionControllerGrpc;
import ru.practicum.messages.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendAction(long userId, long eventId, ActionTypeProto actionType, Instant timestamp) {
        if (client == null) {
            log.warn("gRPC Collector client is null — skipping sendAction");
            return; // Не выбрасываем исключение
        }

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();

        try {
            client.collectUserAction(request);
        } catch (Exception e) {
            log.warn("Ошибка при отправке действия в collector через gRPC: {}", e.getMessage());
        }
    }
}