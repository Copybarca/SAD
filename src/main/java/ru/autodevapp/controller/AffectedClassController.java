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
import ru.autodevapp.controller.dto.affectedclass.AffectedClassRequest;
import ru.autodevapp.controller.dto.affectedclass.AffectedClassResponse;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.service.AffectedClassService;
import ru.autodevapp.view.AffectedClassViewMapper;

import java.net.URI;
import java.util.List;

/**
 * Предоставляет маршруты затронутых классов внутри конкретной задачи.
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/affected-classes")
public class AffectedClassController {

    private final AffectedClassService service;
    private final AffectedClassViewMapper mapper;

    public AffectedClassController(AffectedClassService service, AffectedClassViewMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AffectedClassResponse> create(
            @PathVariable Long taskId, @Valid @RequestBody AffectedClassRequest request) {
        AffectedClass created = service.create(taskId, request.className(), request.changeDescription());
        return ResponseEntity.created(URI.create(
                        "/api/v1/tasks/" + taskId + "/affected-classes/" + created.getId()))
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public List<AffectedClassResponse> findAll(@PathVariable Long taskId) {
        return service.findAll(taskId).stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{classId}")
    public AffectedClassResponse update(
            @PathVariable Long taskId,
            @PathVariable Long classId,
            @Valid @RequestBody AffectedClassRequest request) {
        return mapper.toResponse(service.update(
                taskId, classId, request.className(), request.changeDescription()));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, @PathVariable Long classId) {
        service.delete(taskId, classId);
        return ResponseEntity.noContent().build();
    }
}
