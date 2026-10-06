package io.github.bayramsevim.reservationservice.reservation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservationExpiryJob {
    private final ReservationService reservationService;

    public ReservationExpiryJob(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void expireReservations() {
        int expiredCount = reservationService.expireOverdue();
        if(expiredCount > 0) {
            log.info("Expired " + expiredCount + " reservations.");
        }
    }
}
