package io.github.bayramsevim.reservationservice.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class OutboxRelay {

    private final KafkaTemplate<String,String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;

    public OutboxRelay(KafkaTemplate<String, String> kafkaTemplate, OutboxEventRepository outboxEventRepository, Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload()).join();
            event.markPublished(Instant.now(clock));
        }
    }
}
