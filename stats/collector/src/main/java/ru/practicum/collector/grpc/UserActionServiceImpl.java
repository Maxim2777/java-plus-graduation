package ru.practicum.collector.grpc;

import io.grpc.Status;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.messages.proto.UserActionProto;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.collector.kafka.UserActionProducer;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionServiceImpl extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info(" Received gRPC request: {}", request);

        try {
            long seconds = request.getTimestamp().getSeconds();
            int nanos = request.getTimestamp().getNanos();

            log.info(" Parsed timestamp from proto: seconds = {}, nanos = {}", seconds, nanos);

            Instant instant = Instant.ofEpochSecond(seconds, nanos);
            log.info(" Converted Instant = {}", instant);

            UserActionAvro action = UserActionAvro.newBuilder()
                    .setUserId(request.getUserId())
                    .setEventId(request.getEventId())
                    .setActionType(convertType(request.getActionType()))
                    .setTimestamp(instant)
                    .build();

            log.info(" Built Avro object: {}", action);

            producer.send(action);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error(" Failed to handle gRPC request", e);

            // Обернуть в gRPC статус с сообщением
            StatusRuntimeException statusEx = Status.INTERNAL
                    .withDescription("Failed to handle gRPC request: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException();

            responseObserver.onError(statusEx);
        }
    }

    private ActionTypeAvro convertType(ru.practicum.messages.proto.ActionTypeProto protoType) {
        return switch (protoType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("There is no such type");
        };
    }
}