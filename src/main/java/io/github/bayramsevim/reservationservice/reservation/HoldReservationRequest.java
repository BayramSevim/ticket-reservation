package io.github.bayramsevim.reservationservice.reservation;

import jakarta.validation.constraints.NotNull;

public record HoldReservationRequest(
        @NotNull(message = "userId zorunludur") Long userId,
        @NotNull(message = "seatId zorunludur") Long seatId
) {}