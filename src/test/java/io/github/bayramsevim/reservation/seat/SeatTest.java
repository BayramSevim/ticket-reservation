package io.github.bayramsevim.reservation.seat;

import io.github.bayramsevim.reservation.show.Show;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SeatTest {

    private Show aShow() {
        return new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2026-11-15T18:00:00Z"),
                Instant.parse("2026-10-09T08:00:00Z"),
                Instant.parse("2026-11-15T18:00:00Z"));
    }

    @Test
    void negativePriceIsRejected(){
        assertThrows(IllegalArgumentException.class,()-> new Seat(aShow(), "1A", new BigDecimal("-1")));
    }
}
