package io.github.bayramsevim.reservationservice.reservation;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {
    private static final String TOPIC = "reservation-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishConfirmed(Long reservationId) {
        kafkaTemplate.send(TOPIC, reservationId.toString(),"CONFIRMED:" + reservationId);
    }
}
