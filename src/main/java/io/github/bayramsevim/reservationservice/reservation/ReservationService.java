package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              SeatRepository seatRepository,
                              UserRepository userRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation hold(Long userId, Long seatId){
        User user = userRepository.findById(userId).orElseThrow();
        Seat seat = seatRepository.findById(seatId).orElseThrow();

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofMinutes(10));

        Reservation reservation = new Reservation(seat,user,expiresAt);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirm(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservation.confirm(Instant.now(clock));
        return reservation;
    }

    @Transactional
    public Reservation cancel(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservation.cancel(Instant.now(clock));
        return reservation;
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow();
    }
}
