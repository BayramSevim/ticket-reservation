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
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfig.class)
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


    @Test
    void whenFiftyUsersHoldSameSeatConcurrently_onlyOneSucceeds() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        Seat seat = aSavedSeat();
        List<Long> userIds = new ArrayList<>();
        for(int i = 0; i < 50; i++) {
            userIds.add(aSavedUser().getId());
        }

        for(Long userId : userIds) {
            executor.submit(() -> {
                try {
                    reservationService.hold(userId, seat.getId());
                } catch (Exception e) {
                    // Ignore exceptions for this test
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, reservationRepository.countBySeatId(seat.getId()));
    }

    @Test
    void staleExpireDoesNotOverwriteConfirmedReservation(){
        Seat seat = aSavedSeat();
        User user = aSavedUser();
        ReservationResponse reservation = reservationService.hold(user.getId(), seat.getId());
        Reservation copyA = reservationRepository.findById(reservation.id()).orElseThrow();
        Reservation copyB = reservationRepository.findById(reservation.id()).orElseThrow();

        copyA.confirm(Instant.now());
        reservationRepository.save(copyA);

        copyB.expire(Instant.now().plus(Duration.ofMinutes(11)));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> reservationRepository.save(copyB));

        assertEquals(ReservationStatus.CONFIRMED, reservationRepository.findById(reservation.id()).orElseThrow().getStatus());

    }

    @Test
    void expireOverdueMarksStaleHoldsAsExpired(){
        Seat seat = aSavedSeat();
        User user = aSavedUser();
        Reservation reservation = new Reservation(seat, user,  Instant.now().minusSeconds(60));
        reservationRepository.save(reservation);

        reservationService.expireOverdue();

        assertEquals(ReservationStatus.EXPIRED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());

    }

}
