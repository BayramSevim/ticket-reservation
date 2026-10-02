package io.github.bayramsevim.reservationservice.seat;

import io.github.bayramsevim.reservationservice.exception.NotFoundException;
import io.github.bayramsevim.reservationservice.show.Show;
import io.github.bayramsevim.reservationservice.show.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    public SeatService(SeatRepository seatRepository, ShowRepository showRepository) {
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new NotFoundException("Show not found");
        }
        return seatRepository.findByShowId(showId).stream()
                .map(SeatResponse::from)
                .toList();
    }

    @Transactional
    public SeatResponse addSeats(Long showId, CreateSeatRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Seat seat = seatRepository.save(new Seat(show, request.label(), request.price()));
        return SeatResponse.from(seat);
    }
}
