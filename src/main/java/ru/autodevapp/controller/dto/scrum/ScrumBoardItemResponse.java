package ru.autodevapp.controller.dto.scrum;

import java.time.Instant;

/**
 * Возвращает элемент scrum-доски в transport-формате.
 */
public record ScrumBoardItemResponse(
        Long id,
        String title,
        String description,
        String module,
        Instant createdAt,
        Instant updatedAt
) {
}
