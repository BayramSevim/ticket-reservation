package io.github.bayramsevim.reservationservice.show;

import io.github.bayramsevim.reservationservice.seat.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {
}