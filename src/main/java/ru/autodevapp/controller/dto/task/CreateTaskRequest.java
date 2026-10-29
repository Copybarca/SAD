package ru.autodevapp.controller.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Принимает обязательные поля для создания новой задачи агента.
 */
public record CreateTaskRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String specification,
        @NotBlank @Size(max = 255) String module
) {
}
