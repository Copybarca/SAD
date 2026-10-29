package ru.autodevapp.model.database;

import org.springframework.core.env.Environment;

import java.util.regex.Pattern;

/**
 * Хранит проверенные параметры административного подключения и целевой БД.
 */
public record DatabaseSettings(
        String adminUrl,
        String adminUsername,
        String adminPassword,
        String databaseName
) {

    private static final Pattern DATABASE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");

    public DatabaseSettings {
        if (adminUrl == null || adminUrl.isBlank()) {
            throw new IllegalArgumentException("adminUrl не должен быть пустым");
        }
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalArgumentException("adminUsername не должен быть пустым");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalArgumentException("adminPassword не должен быть пустым");
        }
        if (databaseName == null || !DATABASE_NAME.matcher(databaseName).matches()) {
            throw new IllegalArgumentException(
                    "databaseName должно соответствовать [A-Za-z_][A-Za-z0-9_]{0,62}");
        }
    }

    public static DatabaseSettings from(Environment environment) {
        String adminUrl = required(environment, "app.database.admin-url");
        String adminUsername = required(environment, "app.database.admin-username");
        String adminPassword = required(environment, "app.database.admin-password");
        String databaseName = required(environment, "app.database.name");
        if (!DATABASE_NAME.matcher(databaseName).matches()) {
            throw new IllegalArgumentException(
                    "app.database.name должно соответствовать [A-Za-z_][A-Za-z0-9_]{0,62}");
        }
        return new DatabaseSettings(adminUrl, adminUsername, adminPassword, databaseName);
    }

    private static String required(Environment environment, String property) {
        String value = environment.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Обязательное свойство не задано: " + property);
        }
        return value;
    }
}
