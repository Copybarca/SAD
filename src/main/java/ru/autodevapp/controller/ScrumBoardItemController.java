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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.autodevapp.controller.dto.scrum.ScrumBoardItemRequest;
import ru.autodevapp.controller.dto.scrum.ScrumBoardItemResponse;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.service.ScrumBoardItemService;
import ru.autodevapp.view.ScrumBoardItemViewMapper;

import java.net.URI;
import java.util.List;

/**
 * Предоставляет CRUD-маршруты простой scrum-доски.
 */
@RestController
@RequestMapping("/api/v1/scrum-items")
public class ScrumBoardItemController {

    private final ScrumBoardItemService service;
    private final ScrumBoardItemViewMapper mapper;

    public ScrumBoardItemController(ScrumBoardItemService service, ScrumBoardItemViewMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ScrumBoardItemResponse> create(
            @Valid @RequestBody ScrumBoardItemRequest request) {
        ScrumBoardItem created = service.create(request.title(), request.description(), request.module());
        return ResponseEntity.created(URI.create("/api/v1/scrum-items/" + created.getId()))
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public List<ScrumBoardItemResponse> findAll(@RequestParam(required = false) String module) {
        return service.findAll(module).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{itemId}")
    public ScrumBoardItemResponse findById(@PathVariable Long itemId) {
        return mapper.toResponse(service.findById(itemId));
    }

    @PutMapping("/{itemId}")
    public ScrumBoardItemResponse update(
            @PathVariable Long itemId, @Valid @RequestBody ScrumBoardItemRequest request) {
        return mapper.toResponse(service.update(
                itemId, request.title(), request.description(), request.module()));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long itemId) {
        service.delete(itemId);
        return ResponseEntity.noContent().build();
    }
}
