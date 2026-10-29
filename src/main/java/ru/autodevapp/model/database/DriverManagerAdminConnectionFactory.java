package ru.autodevapp.model.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Создаёт административное подключение стандартным JDBC DriverManager.
 */
public class DriverManagerAdminConnectionFactory implements AdminConnectionFactory {

    @Override
    public Connection open(DatabaseSettings settings) throws SQLException {
        return DriverManager.getConnection(
                settings.adminUrl(), settings.adminUsername(), settings.adminPassword());
    }
}
