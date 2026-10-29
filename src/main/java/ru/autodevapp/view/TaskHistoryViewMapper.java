package ru.autodevapp.view;

import org.springframework.stereotype.Component;
import ru.autodevapp.controller.dto.history.TaskHistoryResponse;
import ru.autodevapp.model.entity.DevelopmentTask;

/**
 * Собирает агрегированную историю задачи для одного API-ответа.
 */
@Component
public class TaskHistoryViewMapper {

    private final DevelopmentTaskViewMapper taskMapper;
    private final AffectedClassViewMapper affectedClassMapper;
    private final TaskActionViewMapper actionMapper;

    public TaskHistoryViewMapper(
            DevelopmentTaskViewMapper taskMapper,
            AffectedClassViewMapper affectedClassMapper,
            TaskActionViewMapper actionMapper) {
        this.taskMapper = taskMapper;
        this.affectedClassMapper = affectedClassMapper;
        this.actionMapper = actionMapper;
    }

    public TaskHistoryResponse toResponse(DevelopmentTask task) {
        return new TaskHistoryResponse(
                taskMapper.toResponse(task),
                task.getAffectedClasses().stream().map(affectedClassMapper::toResponse).toList(),
                task.getActions().stream().map(actionMapper::toResponse).toList());
    }
}
