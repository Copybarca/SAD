package ru.autodevapp.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;
import ru.autodevapp.model.repository.TaskActionRepository;

import java.util.List;

/**
 * Управляет хронологическим журналом действий по задаче.
 */
@Service
public class TaskActionService {

    private final TaskActionRepository repository;
    private final DevelopmentTaskRepository taskRepository;

    public TaskActionService(TaskActionRepository repository, DevelopmentTaskRepository taskRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskAction create(Long taskId, String description, String result) {
        DevelopmentTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача " + taskId + " не найдена"));
        return repository.save(new TaskAction(task, description, result));
    }

    @Transactional(readOnly = true)
    public List<TaskAction> findAll(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Задача " + taskId + " не найдена");
        }
        return repository.findAllByTaskIdOrderByCreatedAtAscIdAsc(taskId);
    }

    @Transactional
    public TaskAction update(Long taskId, Long actionId, String description, String result) {
        TaskAction action = find(taskId, actionId);
        action.update(description, result);
        return action;
    }

    @Transactional
    public void delete(Long taskId, Long actionId) {
        repository.delete(find(taskId, actionId));
    }

    private TaskAction find(Long taskId, Long actionId) {
        return repository.findByIdAndTaskId(actionId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Действие " + actionId + " задачи " + taskId + " не найдено"));
    }
}
