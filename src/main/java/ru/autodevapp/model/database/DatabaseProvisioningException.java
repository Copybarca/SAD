package ru.autodevapp.model.database;

/**
 * Сообщает о невозможности проверить или создать целевую PostgreSQL БД.
 */
public class DatabaseProvisioningException extends RuntimeException {

    public DatabaseProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
