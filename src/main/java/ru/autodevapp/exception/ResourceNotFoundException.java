package ru.autodevapp.exception;

/**
 * Обозначает отсутствие запрошенного API-ресурса в модели.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
