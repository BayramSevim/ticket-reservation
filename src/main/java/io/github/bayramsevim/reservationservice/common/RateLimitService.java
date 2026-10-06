package io.github.bayramsevim.reservationservice.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire(Long userId) {
        String redisKey = "rate:hold:" + userId;
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(WINDOW_SECONDS));
        }
        return currentCount <= LIMIT;
    }
}
