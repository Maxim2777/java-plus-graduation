package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.recommendation.avro.UserAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<Long, UserAction> kafkaTemplate;
    private static final String TOPIC = "stats.user-actions.v1";

    public void send(UserAction action) {
        log.info("Sending action to Kafka: {}", action);
        log.info("Sending Avro as JSON: {}", avroToJson(action)); // подробный лог

        kafkaTemplate.send(TOPIC, action.getEventId(), action); // ключ: Long (eventId)
    }

    // Преобразование Avro → JSON для логов
    private String avroToJson(UserAction record) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<UserAction> writer = new SpecificDatumWriter<>(UserAction.class);
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), out);
            writer.write(record, encoder);
            encoder.flush();
            return out.toString();
        } catch (IOException e) {
            log.error("Failed to serialize Avro to JSON", e);
            return "[error converting to JSON]";
        }
    }
}