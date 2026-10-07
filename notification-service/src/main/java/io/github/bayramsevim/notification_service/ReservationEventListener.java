package io.github.bayramsevim.notification_service;

import io.github.bayramsevim.notification_service.reservation.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservationEventListener {
    @KafkaListener(topics = "reservation-events")
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        log.info("Bilet e-postası gönderiliyor: {} -> {} / {} (rezervasyon {})",
                event.userEmail(), event.showTitle(), event.seatLabel(), event.reservationId());
    }
}
