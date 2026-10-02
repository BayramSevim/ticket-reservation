package io.github.bayramsevim.reservationservice.auth;

import io.github.bayramsevim.reservationservice.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;
    private final Duration refreshExpiration;

    private static final String KEY_PREFIX = "refresh:";

    private String key(String token) {
        return KEY_PREFIX + token;
    }

    public RefreshTokenService(StringRedisTemplate redisTemplate, @Value("${jwt.refresh-expiration}") Duration refreshExpiration
                               ) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiration = refreshExpiration;
    }

    public String create(Long userId){
        String token  = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(token), userId.toString(), refreshExpiration);
        return token;
    }

    public Long consume(String token){
        String userId = redisTemplate.opsForValue().getAndDelete(key(token));
        if(userId == null){
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        return Long.parseLong(userId);
    }

    public void revoke(String token){
        redisTemplate.delete(key(token));
    }
}
