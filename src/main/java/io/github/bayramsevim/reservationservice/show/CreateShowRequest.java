package io.github.bayramsevim.reservationservice.show;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateShowRequest(
        @NotBlank(message = "Başlık boş olamaz")
        String title,
        @NotBlank(message = "Konum boş olamaz")
        String location,
        @NotNull(message = "Başlangıç zamanı boş olamaz")
        Instant startsAt,
        @NotNull(message = "Satış başlangıç zamanı boş olamaz")
        Instant saleStartsAt,
        @NotNull(message = "Satış bitiş zamanı boş olamaz")
        Instant saleEndsAt
) {
}
