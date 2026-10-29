package ru.autodevapp.controller.dto.action;

import java.time.Instant;

/**
 * Возвращает элемент хронологического журнала задачи.
 */
public record TaskActionResponse(
        Long id,
        String actionDescription,
        String actionResult,
        Instant createdAt
) {
}
