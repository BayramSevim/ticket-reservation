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
    public ReservationResponse hold(Long userId, Long seatId){
        User user = userRepository.findById(userId).orElseThrow();
        Seat seat = seatRepository.findById(seatId).orElseThrow();

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofMinutes(10));

        Reservation reservation = new Reservation(seat,user,expiresAt);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse confirm(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservation.confirm(Instant.now(clock));
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservation.cancel(Instant.now(clock));
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long reservationId,Long userId) {
        Reservation reservation =  reservationRepository.findById(reservationId).orElseThrow();
        return ReservationResponse.from(reservation);
    }
}
