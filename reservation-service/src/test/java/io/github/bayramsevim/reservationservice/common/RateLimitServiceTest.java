package io.github.bayramsevim.reservationservice.common;

import io.github.bayramsevim.reservationservice.reservation.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfig.class)
class RateLimitServiceTest {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void allowsTenAttemptsThenRejectsTheEleventh() {
        Long userId = System.nanoTime();

        for (int i = 1; i <= 10; i++) {
            assertTrue(rateLimitService.tryAcquire(userId), "attempt " + i + " should pass");
        }

        assertFalse(rateLimitService.tryAcquire(userId));
    }

    @Test
    void counterGetsAnExpiryOnFirstAttempt() {
        Long userId = System.nanoTime();

        rateLimitService.tryAcquire(userId);

        Long ttl = redisTemplate.getExpire("rate:hold:" + userId);
        assertNotNull(ttl);
        assertTrue(ttl > 0, "counter must expire, but TTL was " + ttl);
    }
}