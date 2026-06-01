package com.alssant.flyway_testcontainers;

import com.alssant.flyway_testcontainers.customer.Customer;
import com.alssant.flyway_testcontainers.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
			assertFalse(connection.isClosed());
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
						"Customer 001"
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

			assertEquals("migration_user", rs.getString(1));
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

			assertEquals("migration_user",  rs.getString(1));
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

	@Test
	void shouldGrantDmlPermissionsToAppUser() throws Exception {

		String sql = """
        SELECT privilege_type
        FROM information_schema.role_table_grants
        WHERE grantee = 'app_user'
          AND table_name = 'customer'
    """;

		try (Connection connection = dataSource.getConnection();
			 Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			Set<String> permissions = new HashSet<>();

			while (rs.next()) {
				permissions.add(rs.getString(1));
			}

			assertThat(permissions)
					.hasSize(4)
					.containsExactlyInAnyOrder("INSERT", "SELECT", "UPDATE", "DELETE");
		}
	}

	@Test
	void shouldConnectAsAppUser() throws Exception {
		final String jdbcUrl = getJdbcUrl();

		try (Connection connection =
					 DriverManager.getConnection(
							 jdbcUrl,
							 "app_user",
							 "app_password")) {

			assertFalse(connection.isClosed());

			var rs = connection.createStatement()
					.executeQuery("select current_user");

			rs.next();
			assertEquals("app_user", rs.getString(1));
		}
	}

	private String getJdbcUrl() throws SQLException {
		return
				dataSource.getConnection()
						.getMetaData()
						.getURL();
	}

	@Test
	void shouldAllowAppUserToInsertCustomer() throws Exception {
		String jdbcUrl = getJdbcUrl();

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "app_user", "app_password")) {
			assertFalse(connection.isClosed());

			connection.setAutoCommit(false);

			String insertSql = "INSERT INTO customer (id, name) VALUES (?, ?)";

			assertThatNoException().isThrownBy(() -> {

				try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
					pstmt.setObject(1, UUID.randomUUID());
					pstmt.setString(2, "Test Customer");

					pstmt.executeUpdate();
				}

			});

			connection.rollback();
		}
	}

	@Test
	void shouldNotAllowAppUserToAlterTable() throws Exception {
		String jdbcUrl = getJdbcUrl();

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "app_user", "app_password")) {
			assertFalse(connection.isClosed());

			String alterSql = "ALTER TABLE customer ADD COLUMN phone VARCHAR(20)";

			assertThatThrownBy(() -> {

				try (Statement stmt = connection.createStatement()) {
					stmt.execute(alterSql);
				}

			}).isInstanceOf(SQLException.class)
					////ansi for  INSUFFICIENT PRIVILEGE like : ERROR: must be owner of table customer, on postgres
					.hasFieldOrPropertyWithValue("SQLState", "42501");

		}
	}



}
