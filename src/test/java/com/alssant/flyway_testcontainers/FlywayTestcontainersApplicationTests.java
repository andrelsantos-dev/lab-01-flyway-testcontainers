package com.alssant.flyway_testcontainers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlywayTestcontainersApplicationTests {
	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldConnectToPostgres() throws Exception{
		try(Connection connection = dataSource.getConnection()) {
			Assertions.assertFalse(connection.isClosed());
		}
	}

	@Test
	void shouldCreateCustomerTable() throws Exception {

		try (Connection connection = dataSource.getConnection()) {

			var rs = connection
					.createStatement()
					.executeQuery("""
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_name = 'customer'
                """);

			assertTrue(rs.next());
		}
	}



}
