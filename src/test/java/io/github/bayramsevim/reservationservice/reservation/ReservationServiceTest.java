package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.common.RateLimitService;
import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.seat.SeatRepository;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final SeatRepository seatRepository = mock(SeatRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SeatHoldService seatHoldService = mock(SeatHoldService.class);
    private final RateLimitService rateLimitService = mock(RateLimitService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T18:00:00Z"), ZoneOffset.UTC);

    private final ReservationService service =
            new ReservationService(reservationRepository, seatRepository, userRepository, clock, seatHoldService, rateLimitService);

    @Test
    void holdCreatesHeldReservationThatExpiresInTenMinutes() {
        Show show = new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2026-11-15T18:00:00Z"),
                Instant.parse("2026-10-09T08:00:00Z"),
                Instant.parse("2026-11-15T18:00:00Z"));
        Seat seat = new Seat(show, "11-A", new BigDecimal("23.40"));
        User user = new User("ali@example.com", "hash");

        when(seatRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seat));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(call -> call.getArgument(0));
        when(seatHoldService.tryHold(1L, 2L)).thenReturn(true);

        ReservationResponse response = service.hold(2L, 1L);

        assertEquals(ReservationStatus.HELD, response.status());
        assertEquals(Instant.parse("2026-10-15T18:10:00Z"), response.expiresAt());
        assertEquals("11-A", response.seatLabel());
        assertEquals("Tarkan Konseri", response.showTitle());
        verify(reservationRepository).save(any(Reservation.class));
    }
}
