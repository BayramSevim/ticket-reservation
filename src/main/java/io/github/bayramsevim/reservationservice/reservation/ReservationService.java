package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

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
}
