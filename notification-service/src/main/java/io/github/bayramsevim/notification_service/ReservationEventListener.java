package io.github.bayramsevim.notification_service;

import io.github.bayramsevim.notification_service.notification.NotificationService;
import io.github.bayramsevim.notification_service.reservation.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {

    private final NotificationService notificationService;

    public ReservationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "reservation-events")
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        notificationService.sendTicketEmail(event);
    }
}