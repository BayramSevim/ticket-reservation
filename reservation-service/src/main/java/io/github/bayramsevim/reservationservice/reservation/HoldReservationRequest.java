package io.github.bayramsevim.reservationservice.reservation;

import jakarta.validation.constraints.NotNull;

public record HoldReservationRequest(
        @NotNull(message = "seatId zorunludur") Long seatId
) {}