package ru.autodevapp.controller.dto.action;

import jakarta.validation.constraints.NotBlank;

/**
 * Принимает краткое действие и необязательный результат наблюдения.
 */
public record TaskActionRequest(
        @NotBlank String actionDescription,
        String actionResult
) {
}
