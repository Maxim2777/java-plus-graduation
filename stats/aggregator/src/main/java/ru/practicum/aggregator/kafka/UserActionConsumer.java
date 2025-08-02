package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.service.AggregatorService;
import ru.practicum.recommendation.avro.UserAction;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final AggregatorService aggregatorService;

    @KafkaListener(
            topics = "${kafka.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, UserAction> record) {
        UserAction userAction = record.value();
        log.info("🔄 Получено действие пользователя: {}", userAction);

        aggregatorService.aggregate(userAction);
    }
}

