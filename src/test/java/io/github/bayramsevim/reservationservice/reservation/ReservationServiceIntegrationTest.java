package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.show.ShowRepository;
import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservationServiceIntegrationTest {

    @Autowired private ReservationService reservationService;
    @Autowired private ShowRepository showRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;

    private Seat aSavedSeat() {
        Show show = showRepository.save(new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2027-11-15T18:00:00Z"),
                Instant.parse("2027-10-09T08:00:00Z"),
                Instant.parse("2027-11-15T18:00:00Z")));
        return seatRepository.save(new Seat(show, "11-A", new BigDecimal("23.40")));
    }

    private User aSavedUser() {
        return userRepository.save(new User(UUID.randomUUID() + "@example.com", "hash"));
    }

    @Test
    void confirmIsPersisted() {
        Seat seat = aSavedSeat();
        User user = aSavedUser();

        ReservationResponse held = reservationService.hold(user.getId(), seat.getId());
        reservationService.confirm(held.id(), user.getId());

        Reservation fromDb = reservationRepository.findById(held.id()).orElseThrow();
        assertEquals(ReservationStatus.CONFIRMED, fromDb.getStatus());
    }

    @Test
    void cancelIsPersisted() {
        Seat seat = aSavedSeat();
        User user = aSavedUser();

        ReservationResponse held = reservationService.hold(user.getId(), seat.getId());
        reservationService.cancel(held.id(), user.getId());


        Reservation fromDb = reservationRepository.findById(held.id()).orElseThrow();
        assertEquals(ReservationStatus.CANCELLED, fromDb.getStatus());
        assertNotNull(fromDb.getCancelledAt());
    }

}
