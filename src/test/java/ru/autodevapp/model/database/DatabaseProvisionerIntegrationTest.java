package ru.autodevapp.model.database;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class DatabaseProvisionerIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("postgres")
            .withUsername("test_admin")
            .withPassword("test_password")
            .withNetworkMode(System.getenv().getOrDefault("TEST_DOCKER_NETWORK", "bridge"));

    DatabaseProvisioner provisioner = new DatabaseProvisioner(new DriverManagerAdminConnectionFactory());

    @Test
    void createsMissingDatabase() throws Exception {
        DatabaseSettings settings = adminSettings("agent_created");

        provisioner.provision(settings);

        assertThat(databaseCount("agent_created")).isEqualTo(1);
    }

    @Test
    void existingDatabaseIsNotCreatedAgain() throws Exception {
        DatabaseSettings settings = adminSettings("agent_existing");
        provisioner.provision(settings);

        provisioner.provision(settings);

        assertThat(databaseCount("agent_existing")).isEqualTo(1);
    }

    @Test
    void roleWithoutCreatedbPrivilegeGetsSafeStartupError() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                adminJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE ROLE limited_user LOGIN PASSWORD 'limited_password' NOCREATEDB");
        }
        DatabaseSettings settings = new DatabaseSettings(
                adminJdbcUrl(), "limited_user", "limited_password", "forbidden_database");

        assertThatThrownBy(() -> provisioner.provision(settings))
                .isInstanceOf(DatabaseProvisioningException.class)
                .hasMessageContaining("forbidden_database")
                .hasMessageNotContaining("limited_password");
    }

    private DatabaseSettings adminSettings(String databaseName) {
        return new DatabaseSettings(
                adminJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), databaseName);
    }

    private int databaseCount(String databaseName) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                adminJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.prepareStatement(
                     "SELECT count(*) FROM pg_database WHERE datname = ?")) {
            statement.setString(1, databaseName);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static String adminJdbcUrl() {
        String networkName = System.getenv("TEST_DOCKER_NETWORK");
        if (networkName == null || networkName.isBlank()) {
            return POSTGRES.getJdbcUrl();
        }
        String host = POSTGRES.getContainerInfo().getNetworkSettings()
                .getNetworks().get(networkName).getIpAddress();
        return "jdbc:postgresql://" + host + ":5432/postgres";
    }
}
