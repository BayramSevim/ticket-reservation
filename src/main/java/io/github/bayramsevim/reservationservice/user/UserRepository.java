package io.github.bayramsevim.reservationservice.user;

import io.github.bayramsevim.reservationservice.seat.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
