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

    public boolean tryHold(Long showId, Long seatId, Long userId) {
        Boolean held = redisTemplate.opsForValue()
                .setIfAbsent(key(showId, seatId), userId.toString(), HOLD_DURATION);
        return Boolean.TRUE.equals(held);
    }

    private String key(Long showId, Long seatId) {
        return "seat:" + showId + ":" + seatId;
    }
}
