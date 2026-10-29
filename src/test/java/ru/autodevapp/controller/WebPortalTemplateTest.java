package ru.autodevapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.service.WebPortalService;
import ru.autodevapp.view.web.DashboardPageView;
import ru.autodevapp.view.web.ScrumPageView;
import ru.autodevapp.view.web.TaskDetailsPageView;
import ru.autodevapp.view.web.TaskListPageView;
import ru.autodevapp.view.web.WebPortalViewMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebPortalController.class)
class WebPortalTemplateTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WebPortalService service;

    @MockitoBean
    WebPortalViewMapper mapper;

    @BeforeEach
    void setUp() {
        when(service.findTasks(null, null)).thenReturn(List.of());
        when(service.findScrumItems(null)).thenReturn(List.of());
    }

    @Test
    void rendersSharedNavigationAndDashboardWidgets() throws Exception {
        DashboardPageView page = new DashboardPageView(
                6, 1, 2, 2, 1, 3,
                List.of(taskSummary()),
                List.of(scrumItem()));
        when(mapper.toDashboardPage(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AUTO DEV")))
                .andExpect(content().string(containsString("Обзор")))
                .andExpect(content().string(containsString("Задачи")))
                .andExpect(content().string(containsString("Scrum")))
                .andExpect(content().string(containsString("Всего задач")))
                .andExpect(content().string(containsString("Анализ генерации")));
    }

    @Test
    void rendersReadOnlyTaskTableWithoutMutationControls() throws Exception {
        TaskListPageView page = new TaskListPageView(
                List.of(taskSummary()),
                "planner-core",
                "COMPLETED",
                List.of("planner-core"),
                List.of(new TaskListPageView.StatusOption("COMPLETED", "Завершена")));
        when(mapper.toTaskListPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.eq("planner-core"),
                org.mockito.ArgumentMatchers.eq(TaskStatus.COMPLETED))).thenReturn(page);

        mockMvc.perform(get("/ui/tasks")
                        .param("module", "planner-core")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Анализ генерации")))
                .andExpect(content().string(containsString("/ui/tasks/6")))
                .andExpect(content().string(not(containsString("/api/v1/tasks"))))
                .andExpect(content().string(not(containsString("method=\"post\""))));
    }

    @Test
    void rendersLongTaskResultAndHistory() throws Exception {
        TaskDetailsPageView page = new TaskDetailsPageView(
                6L, "Анализ генерации", "planner-core", "COMPLETED", "Завершена",
                "status-completed", "29.10.2026 10:00", "29.10.2026 11:00",
                "Первая строка\nВторая строка",
                "Причина установлена\nИсправление описано",
                true,
                List.of(new TaskDetailsPageView.AffectedClassItem(
                        "ru.example.Service", "Изменена логика", "29.10.2026 10:30")),
                List.of(new TaskDetailsPageView.ActionItem(
                        "Проверены логи", "Ошибка найдена", "29.10.2026 10:40")));
        when(mapper.toTaskDetailsPage(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/ui/tasks/6"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Постановка")))
                .andExpect(content().string(containsString("Результат")))
                .andExpect(content().string(containsString("Причина установлена")))
                .andExpect(content().string(containsString("ru.example.Service")))
                .andExpect(content().string(containsString("Проверены логи")))
                .andExpect(content().string(not(containsString("Редактировать"))))
                .andExpect(content().string(not(containsString("Удалить"))));
    }

    @Test
    void rendersScrumCreationFormAndQueue() throws Exception {
        ScrumPageView page = new ScrumPageView(
                List.of(scrumItem()), "", List.of("planner-core"));
        when(mapper.toScrumPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(page);

        mockMvc.perform(get("/ui/scrum"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/ui/scrum\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("Новая задача для агента")))
                .andExpect(content().string(containsString("Проверить API")));
    }

    @Test
    void rendersScrumValidationErrors() throws Exception {
        ScrumPageView page = new ScrumPageView(List.of(), "", List.of());
        when(mapper.toScrumPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(page);

        mockMvc.perform(post("/ui/scrum")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "")
                        .param("description", "")
                        .param("module", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Укажите название")))
                .andExpect(content().string(containsString("Опишите задачу")))
                .andExpect(content().string(containsString("Укажите модуль")));
    }

    @Test
    void servesVisualStylesheet() throws Exception {
        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(containsString("--accent")));
    }

    private TaskListPageView.TaskSummary taskSummary() {
        return new TaskListPageView.TaskSummary(
                6L, "Анализ генерации", "planner-core", "COMPLETED", "Завершена",
                "status-completed", "29.10.2026 11:00");
    }

    private ScrumPageView.ScrumItem scrumItem() {
        return new ScrumPageView.ScrumItem(
                3L, "Проверить API", "Проверить HTTP-поток", "planner-core",
                "29.10.2026 09:00", "29.10.2026 09:00");
    }
}
