package ru.autodevapp.controller.dto.task;

import jakarta.validation.constraints.NotNull;
import ru.autodevapp.model.entity.TaskStatus;

/**
 * Принимает состояние и итог выполнения задачи.
 */
public record UpdateTaskResultRequest(
        @NotNull TaskStatus status,
        String resultDescription
) {
}
