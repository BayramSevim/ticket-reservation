package io.github.bayramsevim.reservationservice.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token boş olamaz")
        String refreshToken
) {
}
