package io.github.bayramsevim.reservationservice.reservation;

import jakarta.validation.constraints.NotNull;

public record HoldReservationRequest(
        @NotNull Long userId,
        @NotNull Long seatId
) {}