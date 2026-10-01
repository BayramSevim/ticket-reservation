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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReservationServiceIntegrationTest {

    @Autowired private ReservationService reservationService;
    @Autowired private ShowRepository showRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;

    @Test
    void confirmIsPersisted() {
        Show show = showRepository.save(new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2027-11-15T18:00:00Z"),
                Instant.parse("2027-10-09T08:00:00Z"),
                Instant.parse("2027-11-15T18:00:00Z")));
        Seat seat = seatRepository.save(new Seat(show, "11-A", new BigDecimal("23.40")));
        User user = userRepository.save(new User(UUID.randomUUID() + "@example.com", "hash"));

        Reservation held = reservationService.hold(user.getId(), seat.getId());
        reservationService.confirm(held.getId());

        Reservation fromDb = reservationRepository.findById(held.getId()).orElseThrow();
        assertEquals(ReservationStatus.CONFIRMED, fromDb.getStatus());
    }
}
