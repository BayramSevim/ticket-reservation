package io.github.bayramsevim.reservationservice;

import io.github.bayramsevim.reservationservice.reservation.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfig.class)
@SpringBootTest
class ReservationApplicationTests {

	@Test
	void contextLoads() {
	}

}
