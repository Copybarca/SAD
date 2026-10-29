package ru.autodevapp.controller.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.autodevapp.model.entity.TaskStatus;

/**
 * Принимает полный набор изменяемых полей основной задачи.
 */
public record UpdateTaskRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String specification,
        @NotBlank @Size(max = 255) String module,
        @NotNull TaskStatus status,
        String resultDescription
) {
}
