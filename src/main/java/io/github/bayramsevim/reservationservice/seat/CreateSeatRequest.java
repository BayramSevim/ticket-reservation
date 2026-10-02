package io.github.bayramsevim.reservationservice.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateSeatRequest(
        @NotBlank
        String label,
        @NotNull
        @PositiveOrZero
        BigDecimal price
) {
}
