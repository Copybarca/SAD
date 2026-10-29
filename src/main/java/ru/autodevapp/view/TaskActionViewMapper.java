package ru.autodevapp.view;

import org.springframework.stereotype.Component;
import ru.autodevapp.controller.dto.action.TaskActionResponse;
import ru.autodevapp.model.entity.TaskAction;

/**
 * Преобразует действие задачи в представление API.
 */
@Component
public class TaskActionViewMapper {

    public TaskActionResponse toResponse(TaskAction action) {
        return new TaskActionResponse(
                action.getId(), action.getActionDescription(), action.getActionResult(), action.getCreatedAt());
    }
}
