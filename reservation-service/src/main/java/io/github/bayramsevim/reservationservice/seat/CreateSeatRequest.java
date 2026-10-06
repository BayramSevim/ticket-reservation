package io.github.bayramsevim.reservationservice.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateSeatRequest(
        @NotBlank(message = "Koltuk etiketi boş olamaz")
        String label,
        @NotNull(message = "Koltuk fiyatı boş olamaz")
        @PositiveOrZero(message = "Koltuk fiyatı negatif olamaz")
        BigDecimal price
) {
}
