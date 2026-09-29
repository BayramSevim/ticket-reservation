package io.github.bayramsevim.reservationservice.reservation;

import io.github.bayramsevim.reservationservice.seat.Seat;
import io.github.bayramsevim.reservationservice.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "reservations")
@Getter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Reservation() {
    }

    public Reservation(Seat seat, User user, Instant expiresAt) {
        this.seat = Objects.requireNonNull(seat, "seat boş olamaz");
        this.user = Objects.requireNonNull(user, "user boş olamaz");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt boş olamaz");
        this.status = ReservationStatus.HELD;
    }

    public void confirm(Instant confirmedAt) {
        if(!(this.status == ReservationStatus.HELD))
            throw new IllegalStateException("Rezervasyon durumu bu durum için confirm olamaz : " + this.status);
        if(!confirmedAt.isBefore(expiresAt))
            throw new IllegalStateException("Rezervasyonun süresi dolmuş. Son geçerlilik: " + expiresAt + ", onay denemesi: " + confirmedAt);

        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public void cancel(Instant cancelledAt){
        if (this.status != ReservationStatus.HELD && this.status != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("Rezervasyon durumu bu durum için cancel olamaz : " + this.status);
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public void expire(Instant now){
        this.status = ReservationStatus.EXPIRED;
    }
}