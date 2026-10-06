package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.show.ShowRepository;
import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTest {

    @Autowired private ShowRepository showRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void savedReservationCanBeFoundWithItsSeat() {
        Instant expiresAt =  Instant.parse("2026-12-20T18:00:00Z");
        Show show = showRepository.save(new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2026-11-15T18:00:00Z"),
                Instant.parse("2026-10-09T08:00:00Z"),
                Instant.parse("2026-11-15T18:00:00Z")));
        Seat seat = seatRepository.save(new Seat(show, "11-A", new BigDecimal("23.40")));
        User user = userRepository.save(new User("ali@example.com", "hash"));
        Reservation reservation = new Reservation(seat,user,expiresAt);
        reservationRepository.save(reservation);
        entityManager.flush();
        entityManager.clear();

        Reservation findReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        System.out.println("===== seat'e dokunmadan ÖNCE =====");

        assertEquals("11-A",findReservation.getSeat().getLabel());

        System.out.println("===== seat'e dokunduktan SONRA =====");
    }
}