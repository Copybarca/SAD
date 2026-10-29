package ru.autodevapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.exception.RestExceptionHandler;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.service.DevelopmentTaskService;
import ru.autodevapp.view.DevelopmentTaskViewMapper;
import ru.autodevapp.view.AffectedClassViewMapper;
import ru.autodevapp.view.TaskActionViewMapper;
import ru.autodevapp.view.TaskHistoryViewMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DevelopmentTaskController.class)
@Import({DevelopmentTaskViewMapper.class, AffectedClassViewMapper.class,
        TaskActionViewMapper.class, TaskHistoryViewMapper.class, RestExceptionHandler.class})
class DevelopmentTaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DevelopmentTaskService service;

    @Test
    void createsTaskAndReturnsLocation() throws Exception {
        when(service.create("API", "Создать маршруты", "planner"))
                .thenReturn(task(10L));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"API","specification":"Создать маршруты","module":"planner"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tasks/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void rejectsBlankRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","specification":"","module":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.specification").exists())
                .andExpect(jsonPath("$.fieldErrors.module").exists());
    }

    @Test
    void returnsFilteredTaskList() throws Exception {
        when(service.findAll("planner", TaskStatus.IN_PROGRESS))
                .thenReturn(List.of(task(11L)));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("module", "planner")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    void missingTaskHasUnifiedNotFoundBody() throws Exception {
        when(service.getHistory(99L)).thenThrow(new ResourceNotFoundException("Задача 99 не найдена"));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Задача 99 не найдена"))
                .andExpect(jsonPath("$.path").value("/api/v1/tasks/99"));
    }

    @Test
    void updatesTaskResult() throws Exception {
        DevelopmentTask task = task(12L);
        task.updateResult(TaskStatus.COMPLETED, "Готово");
        when(service.updateResult(12L, TaskStatus.COMPLETED, "Готово")).thenReturn(task);

        mockMvc.perform(patch("/api/v1/tasks/12/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED","resultDescription":"Готово"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.resultDescription").value("Готово"));
    }

    @Test
    void rejectsUnknownStatus() throws Exception {
        mockMvc.perform(get("/api/v1/tasks").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void returnsAggregatedTaskHistory() throws Exception {
        DevelopmentTask task = task(20L);
        AffectedClass affectedClass = new AffectedClass(task, "TaskService", "Изменён");
        TaskAction action = new TaskAction(task, "Создан API", "Готово");
        ReflectionTestUtils.setField(affectedClass, "id", 2L);
        ReflectionTestUtils.setField(affectedClass, "createdAt", Instant.parse("2026-10-28T07:01:00Z"));
        ReflectionTestUtils.setField(action, "id", 3L);
        ReflectionTestUtils.setField(action, "createdAt", Instant.parse("2026-10-28T07:02:00Z"));
        ReflectionTestUtils.setField(task, "affectedClasses", new java.util.ArrayList<>(List.of(affectedClass)));
        ReflectionTestUtils.setField(task, "actions", new java.util.ArrayList<>(List.of(action)));
        when(service.getHistory(20L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/20/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.id").value(20))
                .andExpect(jsonPath("$.affectedClasses[0].className").value("TaskService"))
                .andExpect(jsonPath("$.actions[0].actionDescription").value("Создан API"));
    }

    @Test
    void taskDetailsIncludeClassesAndActions() throws Exception {
        DevelopmentTask task = task(21L);
        AffectedClass affectedClass = new AffectedClass(task, "DetailsClass", "Изменён");
        TaskAction action = new TaskAction(task, "Details action", null);
        ReflectionTestUtils.setField(affectedClass, "createdAt", Instant.parse("2026-10-28T07:01:00Z"));
        ReflectionTestUtils.setField(action, "createdAt", Instant.parse("2026-10-28T07:02:00Z"));
        ReflectionTestUtils.setField(task, "affectedClasses", new java.util.ArrayList<>(List.of(affectedClass)));
        ReflectionTestUtils.setField(task, "actions", new java.util.ArrayList<>(List.of(action)));
        when(service.getHistory(21L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.id").value(21))
                .andExpect(jsonPath("$.affectedClasses[0].className").value("DetailsClass"))
                .andExpect(jsonPath("$.actions[0].actionDescription").value("Details action"));
    }

    private DevelopmentTask task(Long id) {
        DevelopmentTask task = new DevelopmentTask("API", "Создать маршруты", "planner");
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", Instant.parse("2026-10-28T07:00:00Z"));
        ReflectionTestUtils.setField(task, "updatedAt", Instant.parse("2026-10-28T07:00:00Z"));
        return task;
    }
}
