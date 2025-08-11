package ru.practicum.collector.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageVerifier {

    private static final String TOPIC = "stats.user-actions.v1";

    @EventListener(ApplicationReadyEvent.class)
    public void verifyKafkaMessageAfterStartup() {
        ConsumerFactory<Long, UserActionAvro> consumerFactory = createConsumerFactory();
        try (Consumer<Long, UserActionAvro> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            ConsumerRecords<Long, UserActionAvro> records = consumer.poll(Duration.ofSeconds(5));

            for (ConsumerRecord<Long, UserActionAvro> record : records) {
                log.info("=== Kafka message received ===");
                log.info("Key (eventId): {}", record.key());
                UserActionAvro value = record.value();
                log.info("userId = {}", value.getUserId());
                log.info("eventId = {}", value.getEventId());
                log.info("actionType = {}", value.getActionType());
                log.info("timestamp = {}", value.getTimestamp());
            }
        } catch (Exception e) {
            log.error("Ошибка при чтении сообщений из Kafka", e);
        }
    }

    private ConsumerFactory<Long, UserActionAvro> createConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "debug-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", true);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
