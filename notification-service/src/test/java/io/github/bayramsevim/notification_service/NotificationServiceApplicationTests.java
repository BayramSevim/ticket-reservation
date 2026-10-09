package io.github.bayramsevim.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
