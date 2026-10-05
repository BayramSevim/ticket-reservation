package io.github.bayramsevim.reservationservice.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SeatHoldServiceTest {

    @Autowired
    private SeatHoldService seatHoldService;

    @Test
    void whenFiftyUsersHoldSameSeatConcurrently_onlyOneSucceeds() throws InterruptedException {
        int numberOfUsers = 50;
        long seatId = System.nanoTime();
        AtomicInteger successCount = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        for(int i = 0;i < numberOfUsers; i++){
            long userId = i;
            executorService.submit(()-> {
                if(seatHoldService.tryHold(seatId, userId)){
                    successCount.incrementAndGet();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(1,successCount.get());
    }
}
