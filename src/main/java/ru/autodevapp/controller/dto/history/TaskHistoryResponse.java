package ru.autodevapp.controller.dto.history;

import ru.autodevapp.controller.dto.action.TaskActionResponse;
import ru.autodevapp.controller.dto.affectedclass.AffectedClassResponse;
import ru.autodevapp.controller.dto.task.TaskResponse;

import java.util.List;

/**
 * Возвращает задачу, затронутые классы и журнал одним запросом агента.
 */
public record TaskHistoryResponse(
        TaskResponse task,
        List<AffectedClassResponse> affectedClasses,
        List<TaskActionResponse> actions
) {
}
