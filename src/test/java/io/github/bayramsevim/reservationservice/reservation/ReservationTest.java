package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    private Show aShow() {
        return new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2026-11-15T18:00:00Z"),
                Instant.parse("2026-10-09T08:00:00Z"),
                Instant.parse("2026-11-15T18:00:00Z"));
    }

    private User aUser() {
        return new User("ali@example.com", "123456");
    }

    private Seat aSeat() {
        return new Seat(aShow(), "11-A", new BigDecimal("23.40"));
    }

    @Test
    void newReservationIsHeld() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        assertEquals(ReservationStatus.HELD, reservation.getStatus());
    }

    @Test
    void heldReservationCanBeConfirmed() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        reservation.confirm(now);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void confirmedReservationCannotBeConfirmedAgain(){
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);
        reservation.confirm(now);
        assertThrows(IllegalStateException.class, ()-> reservation.confirm(now));
    }

    @Test
    void confirmingSetsConfirmedAt() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        reservation.confirm(now);

        assertEquals(now, reservation.getConfirmedAt());
    }

    @Test
    void expiredReservationCannotBeConfirmed() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:15:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        assertThrows(IllegalStateException.class,()-> reservation.confirm(now));
    }

    @Test
    void heldReservationCanBeCancelled() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        reservation.cancel(now);
        assertEquals(ReservationStatus.CANCELLED,reservation.getStatus());
        assertEquals(reservation.getCancelledAt(),now);
    }

    @Test
    void cancelledReservationCannotBeCancelledAgain() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);

        reservation.cancel(now);

        assertThrows(IllegalStateException.class, () -> reservation.cancel(now));
    }

    @Test
    void confirmedReservationCanBeCancelled() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);
        reservation.confirm(now);
        reservation.cancel(now);
        assertEquals(ReservationStatus.CANCELLED,reservation.getStatus());
    }

    @Test
    void heldReservationExpiresAfterDeadline() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:15:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);
        reservation.expire(now);
        assertEquals(ReservationStatus.EXPIRED,reservation.getStatus());
    }

    @Test
    void reservationCannotExpireBeforeDeadline() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant now       = Instant.parse("2026-10-15T18:05:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);
        assertThrows(IllegalStateException.class, () -> reservation.expire(now));
    }

    @Test
    void confirmedReservationCannotExpire() {
        Instant expiresAt = Instant.parse("2026-10-15T18:10:00Z");
        Instant paidAt    = Instant.parse("2026-10-15T18:05:00Z");
        Instant later     = Instant.parse("2026-10-15T18:15:00Z");
        Reservation reservation = new Reservation(aSeat(), aUser(), expiresAt);
        reservation.confirm(paidAt);

        assertThrows(IllegalStateException.class, () -> reservation.expire(later));
    }
}