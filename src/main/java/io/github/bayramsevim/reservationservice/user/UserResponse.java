package io.github.bayramsevim.reservationservice.user;

public record UserResponse(
    Long id,
    String email,
    Role role,
    String password
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getPasswordHash()
        );
    }
}
