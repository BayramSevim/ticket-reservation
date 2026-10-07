package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.outbox.OutboxEvent;
import io.github.bayramsevim.reservationservice.outbox.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class ReservationEventPublisher {
    private static final String TOPIC = "reservation-events";

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public ReservationEventPublisher(OutboxEventRepository outboxEventRepository, JsonMapper jsonMapper, Clock clock) {
        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    public void publishConfirmed(Reservation reservation) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now(clock);
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(eventId, reservation.getId(),
                reservation.getUser().getEmail(), reservation.getSeat().getShow().getTitle(),
                reservation.getSeat().getLabel(), now);
        outboxEventRepository.save(new OutboxEvent(eventId, TOPIC, reservation.getId().toString(),jsonMapper.writeValueAsString(event), now));
    }
}
