package io.github.bayramsevim.reservationservice.show;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ShowRepositoryTest {

    @Autowired
    private ShowRepository showRepository;

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

        Show findShow = showRepository.findById(savedShow.getId()).orElseThrow();

        assertEquals(savedShow.getTitle(),findShow.getTitle());
    }
}