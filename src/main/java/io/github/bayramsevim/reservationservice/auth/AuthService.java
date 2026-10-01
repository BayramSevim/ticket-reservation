package io.github.bayramsevim.reservationservice.auth;

import io.github.bayramsevim.reservationservice.exception.InvalidCredentialsException;
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
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }


    @Transactional
    public UserResponse register(RegisterRequest request) {
        boolean exists = userRepository.existsByEmail(request.email());
        if(exists)
            throw new IllegalStateException("Email zaten kayıtlı");

        return UserResponse.from(userRepository.save(new User(request.email(), passwordEncoder.encode(request.password()))));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new InvalidCredentialsException("Şifre hatalı");

        String token =  tokenService.generateToken(user);

        return new LoginResponse(token, tokenService.expiresInSeconds());
    }
}
