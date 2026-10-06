package io.github.bayramsevim.reservationservice.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        String refreshToken
) {

}
