package io.github.bayramsevim.reservationservice.reservation;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        ReservationStatus status,
        String seatLabel,
        String showTitle,
        Instant expiresAt,
        Instant confirmedAt,
        Instant cancelledAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getSeat().getLabel(),
                reservation.getSeat().getShow().getTitle(),
                reservation.getExpiresAt(),
                reservation.getConfirmedAt(),
                reservation.getCancelledAt()
        );
    }
}