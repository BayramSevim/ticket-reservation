package io.github.bayramsevim.reservationservice.auth;

import io.github.bayramsevim.reservationservice.user.User;
import io.github.bayramsevim.reservationservice.user.UserRepository;
import io.github.bayramsevim.reservationservice.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public UserResponse register(RegisterRequest request) {
        boolean exists = userRepository.existsByEmail(request.email());
        if(exists)
            throw new IllegalStateException("Email zaten kayıtlı");

        return UserResponse.from(userRepository.save(new User(request.email(), passwordEncoder.encode(request.password()))));
    }
}
