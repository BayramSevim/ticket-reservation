package io.github.bayramsevim.reservation.show;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "shows")
@Getter
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "location")
    private String location;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "sale_starts_at")
    private Instant saleStartsAt;

    @Column(name = "sale_ends_at")
    private Instant saleEndsAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Show() {
    }

    public Show(String title, String location, Instant startsAt, Instant saleStartsAt, Instant saleEndsAt) {

        if(!saleEndsAt.isAfter(saleStartsAt))
            throw new IllegalArgumentException("Satış bitişi, satış başlangıcından sonra olmalı");
        if(saleEndsAt.isAfter(startsAt))
            throw new IllegalArgumentException("Satış, en geç konser başladığında bitmeli");


        this.title = title;
        this.location = location;
        this.saleEndsAt = saleEndsAt;
        this.saleStartsAt = saleStartsAt;
        this.startsAt = startsAt;
    }
}
