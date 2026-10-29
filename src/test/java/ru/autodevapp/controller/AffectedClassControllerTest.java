package ru.autodevapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ru.autodevapp.exception.RestExceptionHandler;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.service.AffectedClassService;
import ru.autodevapp.view.AffectedClassViewMapper;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffectedClassController.class)
@Import({AffectedClassViewMapper.class, RestExceptionHandler.class})
class AffectedClassControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AffectedClassService service;

    @Test
    void addsAffectedClassToTask() throws Exception {
        AffectedClass affectedClass = new AffectedClass(
                new DevelopmentTask("Task", "Spec", "planner"), "TaskService", "Изменён");
        ReflectionTestUtils.setField(affectedClass, "id", 4L);
        ReflectionTestUtils.setField(affectedClass, "createdAt", Instant.parse("2026-10-28T07:00:00Z"));
        when(service.create(1L, "TaskService", "Изменён")).thenReturn(affectedClass);

        mockMvc.perform(post("/api/v1/tasks/1/affected-classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"className":"TaskService","changeDescription":"Изменён"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.className").value("TaskService"));
    }

    @Test
    void rejectsBlankAffectedClass() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/1/affected-classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"className":" ","changeDescription":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.className").exists());
    }
}
