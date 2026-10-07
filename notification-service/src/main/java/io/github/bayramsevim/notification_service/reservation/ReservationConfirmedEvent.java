package io.github.bayramsevim.notification_service.reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationConfirmedEvent(
        UUID eventId,
        Long reservationId,
        String userEmail,
        String showTitle,
        String seatLabel,
        Instant occurredAt) {
}