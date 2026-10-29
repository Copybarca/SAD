package ru.autodevapp.controller.dto.task;

import java.time.Instant;

/**
 * Возвращает основные поля задачи в стабильном transport-формате.
 */
public record TaskResponse(
        Long id,
        String title,
        String specification,
        String module,
        String resultDescription,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
