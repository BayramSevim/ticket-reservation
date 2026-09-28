package io.github.bayramsevim.reservation.show;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShowTest {

    @Test
    void saleEndingBeforeItStartsIsRejected(){
        Instant startsAt = Instant.parse("2026-11-15T18:00:00Z");
        Instant saleStartsAt = Instant.parse("2026-10-09T08:00:00Z");
        Instant saleEndsAt   = Instant.parse("2026-10-01T23:59:59Z");


        assertThrows(IllegalArgumentException.class, () -> {
            new Show("Tarkan Konseri","Harbiye",startsAt,saleStartsAt,saleEndsAt);
        });
    }

    @Test
    void saleStartingAndEndingAtSameMomentIsRejected(){
        Instant startsAt = Instant.parse("2026-11-15T18:00:00Z");
        Instant saleStartsAt = Instant.parse("2026-10-09T08:00:00Z");
        Instant saleEndsAt   = Instant.parse("2026-10-09T08:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> {
            new Show("Tarkan Konseri","Harbiye",startsAt,saleStartsAt,saleEndsAt);
        });
    }
}
