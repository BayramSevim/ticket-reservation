package io.github.bayramsevim.reservationservice.show;

import io.github.bayramsevim.reservationservice.reservation.TestcontainersConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;


import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
public class ShowRepositoryTest {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EntityManager entityManager;

    private Show aShow() {
        return new Show("Tarkan Konseri", "Harbiye",
                Instant.parse("2026-11-15T18:00:00Z"),
                Instant.parse("2026-10-09T08:00:00Z"),
                Instant.parse("2026-11-15T18:00:00Z"));
    }


    @Test
    void savedShowCanBeFoundById() {
        Show show = aShow();
        Show savedShow = showRepository.save(show);

        entityManager.flush();
        entityManager.clear();

        Show findShow = showRepository.findById(savedShow.getId()).orElseThrow();

        assertEquals(savedShow.getTitle(),findShow.getTitle());
        assertEquals(savedShow.getLocation(),findShow.getLocation());
        assertEquals(savedShow.getStartsAt(),findShow.getStartsAt());

    }
}