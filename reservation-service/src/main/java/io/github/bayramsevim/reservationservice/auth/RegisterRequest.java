package io.github.bayramsevim.reservationservice.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email alanı boş bırakılamaz")
        @Email(message = "Geçersiz e-posta formatı")
        String email,
        @NotBlank(message = "Şifre alanı boş bırakılamaz")
        @Size(min = 8,message = "Şifre en az 8 karakter olmalıdır")
        String password
) {
}
