package io.github.bayramsevim.reservationservice.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email boş olamaz")
        String email,
        @NotBlank(message = "Şifre boş olamaz")
        String password
) {
}
