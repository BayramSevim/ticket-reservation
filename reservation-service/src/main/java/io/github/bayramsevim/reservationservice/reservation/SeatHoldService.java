package io.github.bayramsevim.reservationservice.reservation;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SeatHoldService {

    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);
    private final StringRedisTemplate redisTemplate;

    public SeatHoldService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryHold(Long seatId, Long userId) {
        Boolean held = redisTemplate.opsForValue()
                .setIfAbsent(key(seatId), userId.toString(), HOLD_DURATION);
        return Boolean.TRUE.equals(held);
    }

    public void release(Long seatId) {
        redisTemplate.delete(key(seatId));
    }

    private String key(Long seatId) {
        return "seat:" + seatId;
    }
}
