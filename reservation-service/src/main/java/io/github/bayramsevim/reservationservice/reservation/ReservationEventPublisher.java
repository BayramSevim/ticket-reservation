package io.github.bayramsevim.reservationservice.reservation;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class ReservationEventPublisher {
    private static final String TOPIC = "reservation-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    public ReservationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    public void publishConfirmed(Reservation reservation) {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                UUID.randomUUID(),
                reservation.getId(),
                reservation.getUser().getEmail(),
                reservation.getSeat().getShow().getTitle(),
                reservation.getSeat().getLabel(),
                Instant.now(clock));
        kafkaTemplate.send(TOPIC, reservation.getId().toString(), event);
    }
}
