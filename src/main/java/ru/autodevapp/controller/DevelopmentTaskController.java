package ru.autodevapp.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.autodevapp.controller.dto.task.CreateTaskRequest;
import ru.autodevapp.controller.dto.task.TaskResponse;
import ru.autodevapp.controller.dto.task.UpdateTaskRequest;
import ru.autodevapp.controller.dto.task.UpdateTaskResultRequest;
import ru.autodevapp.controller.dto.history.TaskHistoryResponse;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.service.DevelopmentTaskService;
import ru.autodevapp.view.DevelopmentTaskViewMapper;
import ru.autodevapp.view.TaskHistoryViewMapper;

import java.net.URI;
import java.util.List;

/**
 * Предоставляет CRUD-маршруты для основных задач агентов.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class DevelopmentTaskController {

    private final DevelopmentTaskService service;
    private final DevelopmentTaskViewMapper mapper;
    private final TaskHistoryViewMapper historyMapper;

    public DevelopmentTaskController(
            DevelopmentTaskService service,
            DevelopmentTaskViewMapper mapper,
            TaskHistoryViewMapper historyMapper) {
        this.service = service;
        this.mapper = mapper;
        this.historyMapper = historyMapper;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        DevelopmentTask task = service.create(request.title(), request.specification(), request.module());
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + task.getId()))
                .body(mapper.toResponse(task));
    }

    @GetMapping
    public List<TaskResponse> findAll(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) TaskStatus status) {
        return service.findAll(module, status).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{taskId}")
    public TaskHistoryResponse findById(@PathVariable Long taskId) {
        return historyMapper.toResponse(service.getHistory(taskId));
    }

    @GetMapping("/{taskId}/history")
    public TaskHistoryResponse history(@PathVariable Long taskId) {
        return historyMapper.toResponse(service.getHistory(taskId));
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return mapper.toResponse(service.update(
                taskId,
                request.title(),
                request.specification(),
                request.module(),
                request.status(),
                request.resultDescription()));
    }

    @PatchMapping("/{taskId}/result")
    public TaskResponse updateResult(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskResultRequest request) {
        return mapper.toResponse(service.updateResult(
                taskId, request.status(), request.resultDescription()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId) {
        service.delete(taskId);
        return ResponseEntity.noContent().build();
    }
}
