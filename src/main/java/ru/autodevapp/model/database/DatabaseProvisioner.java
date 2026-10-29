package ru.autodevapp.model.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Проверяет наличие целевой PostgreSQL БД и создаёт её при необходимости.
 */
public class DatabaseProvisioner {

    private final AdminConnectionFactory connectionFactory;

    public DatabaseProvisioner(AdminConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void provision(DatabaseSettings settings) {
        try (Connection connection = connectionFactory.open(settings)) {
            if (!databaseExists(connection, settings.databaseName())) {
                createDatabase(connection, settings.databaseName());
            }
        } catch (SQLException exception) {
            throw new DatabaseProvisioningException(
                    "Не удалось проверить или создать БД " + settings.databaseName(), exception);
        }
    }

    private boolean databaseExists(Connection connection, String databaseName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM pg_database WHERE datname = ?")) {
            statement.setString(1, databaseName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void createDatabase(Connection connection, String databaseName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE \"" + databaseName + "\"");
        }
    }
}
