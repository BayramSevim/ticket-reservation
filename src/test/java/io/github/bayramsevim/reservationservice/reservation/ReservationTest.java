package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}