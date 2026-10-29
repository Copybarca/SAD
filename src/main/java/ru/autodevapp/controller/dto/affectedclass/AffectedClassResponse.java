package ru.autodevapp.controller.dto.affectedclass;

import java.time.Instant;

/**
 * Возвращает запись о классе, затронутом задачей.
 */
public record AffectedClassResponse(
        Long id,
        String className,
        String changeDescription,
        Instant createdAt
) {
}
