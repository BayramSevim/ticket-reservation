package io.github.bayramsevim.reservationservice.show;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void saleEndingAfterShowStartsIsRejected(){
        Instant startsAt = Instant.parse("2026-11-15T18:00:00Z");
        Instant saleStartsAt = Instant.parse("2026-10-09T08:00:00Z");
        Instant saleEndsAt   = Instant.parse("2026-11-16T08:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> {
            new Show("Tarkan Konseri","Harbiye",startsAt,saleStartsAt,saleEndsAt);
        });
    }

    @Test
    void validShowIsCreatedEvenWhenSaleEndsExactlyAtShowStart(){
        Instant startsAt = Instant.parse("2026-11-15T18:00:00Z");
        Instant saleStartsAt = Instant.parse("2026-10-09T08:00:00Z");
        Instant saleEndsAt   = Instant.parse("2026-11-15T18:00:00Z");

        Show show = new Show("Tarkan Konseri","Harbiye",startsAt,saleStartsAt,saleEndsAt);
        assertEquals("Tarkan Konseri", show.getTitle());
        assertEquals(saleEndsAt, show.getSaleEndsAt());
    }
}
