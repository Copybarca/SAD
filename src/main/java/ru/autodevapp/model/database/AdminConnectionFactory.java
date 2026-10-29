package ru.autodevapp.model.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Открывает административное JDBC-подключение для проверки и создания БД.
 */
@FunctionalInterface
public interface AdminConnectionFactory {

    Connection open(DatabaseSettings settings) throws SQLException;
}
