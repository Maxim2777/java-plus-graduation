package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.recommendation.avro.UserActionAvro;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;
    private static final String TOPIC = "stats.user-actions.v1";

    public void send(UserActionAvro action) {
        log.info("Sending action to Kafka: {}", action);

        long timestampMs = action.getTimestamp().toEpochMilli(); // <-- ключевая строка

        ProducerRecord<Long, UserActionAvro> record = new ProducerRecord<>(
                TOPIC,
                null,             // partition (можно null)
                action.getTimestamp().toEpochMilli(),     // <-- обязательно long
                action.getEventId(),
                action
        );

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Message sent to Kafka topic {} with offset {}", result.getRecordMetadata().topic(), result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to send message to Kafka", ex);
            }
        });
    }

    // Преобразование Avro → JSON для логов
    private String avroToJson(UserActionAvro record) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<UserActionAvro> writer = new SpecificDatumWriter<>(UserActionAvro.class);
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