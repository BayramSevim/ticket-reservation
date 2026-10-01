package io.github.bayramsevim.reservationservice.user;

import io.github.bayramsevim.reservationservice.seat.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
}
