package io.github.bayramsevim.reservationservice.reservation;

public record HoldReservationRequest(
        Long userId,
        Long seatId
) {
}
