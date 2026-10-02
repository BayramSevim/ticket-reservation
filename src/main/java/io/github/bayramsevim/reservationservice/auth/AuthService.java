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
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
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
                .orElseThrow(() -> new InvalidCredentialsException("E-posta veya şifre hatalı"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new InvalidCredentialsException("E-posta veya şifre hatalı");

        String token =  tokenService.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getId());

        return new LoginResponse(token, tokenService.expiresInSeconds(), refreshToken);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        Long userId = refreshTokenService.consume(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        String token = tokenService.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getId());

        return new LoginResponse(token, tokenService.expiresInSeconds(), refreshToken);
    }

    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
