package com.alssant.flyway_testcontainers;

import com.alssant.flyway_testcontainers.customer.Customer;
import com.alssant.flyway_testcontainers.customer.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlywayTestcontainersApplicationTests {
	@Autowired
	private DataSource dataSource;

	@Autowired
	private CustomerRepository repository;

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

	@Test
	void shouldSaveCustomer() {

		Customer customer =
				new Customer(
						UUID.randomUUID(),
						"Andre"
				);

		repository.save(customer);

		assertEquals(1, repository.count());
	}

	@Test
	void shouldShowCurrentDatabaseUser() throws Exception {

		try (Connection connection = dataSource.getConnection()) {

			var rs = connection.createStatement()
					.executeQuery("select current_user");

			rs.next();

			System.out.println(
					"Current user = " + rs.getString(1)
			);
		}
	}

	@Test
	void shouldShowCustomerTableOwner() throws Exception {

		try (Connection connection = dataSource.getConnection()) {

			var rs = connection.createStatement().executeQuery("""
            SELECT tableowner
            FROM pg_tables
            WHERE tablename = 'customer'
        """);

			rs.next();

			System.out.println(
					"Customer owner = " + rs.getString(1)
			);
		}
	}

	@Test
	void shouldCreateAppUser() throws Exception {

		try (Connection connection = dataSource.getConnection()) {

			var rs = connection.createStatement().executeQuery("""
            SELECT rolname
            FROM pg_roles
            WHERE rolname = 'app_user'
        """);

			assertTrue(rs.next());
		}
	}



}
