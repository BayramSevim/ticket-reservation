package io.github.bayramsevim.reservation.seat;

import io.github.bayramsevim.reservation.show.Show;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "seats")
@Getter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "show_id")
    private Show show;

    @Column(name = "label")
    private String label;

    @Column(name = "price")
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Seat() {
    }

    public Seat(Show show,String label, BigDecimal price) {
        if(price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Tutar 0 dan küçük olamaz");

        this.label = label;
        this.price = price;
        this.show = show;
    }
}
