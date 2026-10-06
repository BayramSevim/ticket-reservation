package io.github.bayramsevim.reservationservice.auth;

import io.github.bayramsevim.reservationservice.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration expiration;

    public TokenService(JwtEncoder jwtEncoder, Clock clock,
                        @Value("${jwt.expiration}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        Instant now = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("reservation-service")
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return expiration.toSeconds();
    }
}