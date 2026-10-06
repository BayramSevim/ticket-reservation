package io.github.bayramsevim.reservationservice.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitService {
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) " +
                    "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                    "return c",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire(Long userId) {
        String redisKey = "rate:hold:" + userId;
        Long currentCount = redisTemplate.execute(
                INCREMENT_SCRIPT,
                List.of(redisKey),
                String.valueOf(WINDOW_SECONDS));
        return currentCount <= LIMIT;
    }
}
