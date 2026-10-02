package io.github.bayramsevim.reservationservice.seat;

import io.github.bayramsevim.reservationservice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByShowId(Long showId);
}
