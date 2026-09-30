package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
}
