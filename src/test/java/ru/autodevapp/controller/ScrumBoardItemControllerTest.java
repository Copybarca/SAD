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
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.service.ScrumBoardItemService;
import ru.autodevapp.view.ScrumBoardItemViewMapper;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScrumBoardItemController.class)
@Import({ScrumBoardItemViewMapper.class, RestExceptionHandler.class})
class ScrumBoardItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ScrumBoardItemService service;

    @Test
    void createsScrumItem() throws Exception {
        ScrumBoardItem item = new ScrumBoardItem("API", "Добавить маршруты", "planner");
        ReflectionTestUtils.setField(item, "id", 6L);
        ReflectionTestUtils.setField(item, "createdAt", Instant.parse("2026-10-28T07:00:00Z"));
        ReflectionTestUtils.setField(item, "updatedAt", Instant.parse("2026-10-28T07:00:00Z"));
        when(service.create("API", "Добавить маршруты", "planner")).thenReturn(item);

        mockMvc.perform(post("/api/v1/scrum-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"API","description":"Добавить маршруты","module":"planner"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6));
    }

    @Test
    void rejectsBlankScrumFields() throws Exception {
        mockMvc.perform(post("/api/v1/scrum-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","description":"","module":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.module").exists());
    }
}
