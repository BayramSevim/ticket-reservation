package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    /* @Query("""
        select r from Reservation r
        join fetch r.seat s
        join fetch s.show
        where r.user.id = :userId
        """) */
    @EntityGraph(attributePaths = {"seat", "seat.show"})
    List<Reservation> findByUserId(@Param("userId") Long userId);

    boolean existsBySeatIdAndStatusIn(Long seatId, List<ReservationStatus> statuses);

    long countBySeatId(Long seatId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant expiresAt);

}
