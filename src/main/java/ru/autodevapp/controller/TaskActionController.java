package ru.autodevapp.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.autodevapp.controller.dto.action.TaskActionRequest;
import ru.autodevapp.controller.dto.action.TaskActionResponse;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.service.TaskActionService;
import ru.autodevapp.view.TaskActionViewMapper;

import java.net.URI;
import java.util.List;

/**
 * Предоставляет маршруты журнала действий внутри конкретной задачи.
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/actions")
public class TaskActionController {

    private final TaskActionService service;
    private final TaskActionViewMapper mapper;

    public TaskActionController(TaskActionService service, TaskActionViewMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TaskActionResponse> create(
            @PathVariable Long taskId, @Valid @RequestBody TaskActionRequest request) {
        TaskAction created = service.create(taskId, request.actionDescription(), request.actionResult());
        return ResponseEntity.created(URI.create(
                        "/api/v1/tasks/" + taskId + "/actions/" + created.getId()))
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public List<TaskActionResponse> findAll(@PathVariable Long taskId) {
        return service.findAll(taskId).stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{actionId}")
    public TaskActionResponse update(
            @PathVariable Long taskId,
            @PathVariable Long actionId,
            @Valid @RequestBody TaskActionRequest request) {
        return mapper.toResponse(service.update(
                taskId, actionId, request.actionDescription(), request.actionResult()));
    }

    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, @PathVariable Long actionId) {
        service.delete(taskId, actionId);
        return ResponseEntity.noContent().build();
    }
}
