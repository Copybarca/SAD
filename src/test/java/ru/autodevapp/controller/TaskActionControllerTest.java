package ru.autodevapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ru.autodevapp.exception.RestExceptionHandler;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.service.TaskActionService;
import ru.autodevapp.view.TaskActionViewMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskActionController.class)
@Import({TaskActionViewMapper.class, RestExceptionHandler.class})
class TaskActionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TaskActionService service;

    @Test
    void readsActionsInServiceOrder() throws Exception {
        TaskAction first = action(1L, "Первое");
        TaskAction second = action(2L, "Второе");
        when(service.findAll(5L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/tasks/5/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actionDescription").value("Первое"))
                .andExpect(jsonPath("$[1].actionDescription").value("Второе"));
    }

    private TaskAction action(Long id, String description) {
        TaskAction action = new TaskAction(null, description, null);
        ReflectionTestUtils.setField(action, "id", id);
        ReflectionTestUtils.setField(action, "createdAt", Instant.parse("2026-10-28T07:00:00Z"));
        return action;
    }
}
