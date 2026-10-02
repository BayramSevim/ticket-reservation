package io.github.bayramsevim.reservationservice.seat;

import java.math.BigDecimal;

public record SeatResponse(
    Long id,
    String label,
    BigDecimal price
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getLabel(),
                seat.getPrice()
        );
    }
}
