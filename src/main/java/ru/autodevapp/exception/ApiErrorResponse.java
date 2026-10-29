package ru.autodevapp.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Задаёт единый JSON-формат ошибок всех REST-маршрутов.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
