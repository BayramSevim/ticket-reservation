package io.github.bayramsevim.notification_service.notification;

import io.github.bayramsevim.notification_service.reservation.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
public class NotificationService {

    private final ProcessedEventRepository processedEventRepository;

    public NotificationService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void sendTicketEmail(ReservationConfirmedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Olay zaten işlenmiş, atlanıyor: {}", event.eventId());
            return;
        }
        processedEventRepository.save(new ProcessedEvent(event.eventId(), Instant.now()));
        log.info("Bilet e-postası gönderiliyor: {} -> {} / {} (rezervasyon {})",
                event.userEmail(), event.showTitle(), event.seatLabel(), event.reservationId());
    }
}
