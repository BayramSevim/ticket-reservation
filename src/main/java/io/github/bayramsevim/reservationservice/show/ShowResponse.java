package io.github.bayramsevim.reservationservice.show;
import java.time.Instant;

public record ShowResponse(
        Long id,
        String title,
        String location,
        Instant startsAt,
        Instant saleStartsAt,
        Instant saleEndsAt
) {
    public static ShowResponse from(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getTitle(),
                show.getLocation(),
                show.getStartsAt(),
                show.getSaleStartsAt(),
                show.getSaleEndsAt()
        );
    }
}
