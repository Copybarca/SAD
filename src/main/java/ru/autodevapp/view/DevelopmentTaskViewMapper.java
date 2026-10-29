package ru.autodevapp.view;

import org.springframework.stereotype.Component;
import ru.autodevapp.controller.dto.task.TaskResponse;
import ru.autodevapp.model.entity.DevelopmentTask;

/**
 * Преобразует основную задачу в JSON-представление API.
 */
@Component
public class DevelopmentTaskViewMapper {

    public TaskResponse toResponse(DevelopmentTask task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getSpecification(),
                task.getModule(),
                task.getResultDescription(),
                task.getStatus().name(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
