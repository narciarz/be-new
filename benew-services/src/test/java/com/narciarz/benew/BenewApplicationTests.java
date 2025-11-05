package com.narciarz.benew;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BenewApplicationTests {

	@Container
	@ServiceConnection
	@SuppressWarnings("resource") // Container lifecycle is managed by Testcontainers
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("benew_test")
			.withUsername("test")
			.withPassword("test");

	@Test
	void contextLoads() {
	}

}
