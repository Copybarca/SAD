package ru.autodevapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.service.WebPortalService;
import ru.autodevapp.view.web.DashboardPageView;
import ru.autodevapp.view.web.ScrumPageView;
import ru.autodevapp.view.web.TaskDetailsPageView;
import ru.autodevapp.view.web.TaskListPageView;
import ru.autodevapp.view.web.WebPortalViewMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = WebPortalController.class, properties = "spring.thymeleaf.enabled=false")
class WebPortalControllerTest {

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
        when(mapper.toTaskListPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.nullable(TaskStatus.class)))
                .thenReturn(emptyTaskList());
        when(mapper.toScrumPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class)))
                .thenReturn(emptyScrumPage());
    }

    @Test
    void redirectsRootToOverview() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui"));
    }

    @Test
    void rendersOverview() throws Exception {
        DashboardPageView page = new DashboardPageView(0, 0, 0, 0, 0, 0, List.of(), List.of());
        when(mapper.toDashboardPage(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("page", page))
                .andExpect(model().attribute("activePage", "dashboard"));
    }

    @Test
    void passesCombinedFiltersToTaskUseCase() throws Exception {
        TaskListPageView page = emptyTaskList();
        when(service.findTasks("planner-core", TaskStatus.COMPLETED)).thenReturn(List.of());
        when(mapper.toTaskListPage(anyList(), anyList(),
                org.mockito.ArgumentMatchers.eq("planner-core"),
                org.mockito.ArgumentMatchers.eq(TaskStatus.COMPLETED))).thenReturn(page);

        mockMvc.perform(get("/ui/tasks")
                        .param("module", "planner-core")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attribute("page", page));

        verify(service).findTasks("planner-core", TaskStatus.COMPLETED);
    }

    @Test
    void rendersTaskDetails() throws Exception {
        TaskDetailsPageView page = taskDetails();
        when(mapper.toTaskDetailsPage(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/ui/tasks/6"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/details"))
                .andExpect(model().attribute("page", page));
    }

    @Test
    void rendersHtmlNotFoundForUnknownTask() throws Exception {
        when(service.findTask(404L)).thenThrow(new ResourceNotFoundException("Задача 404 не найдена"));

        mockMvc.perform(get("/ui/tasks/404"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/not-found"))
                .andExpect(model().attribute("message", "Задача 404 не найдена"));
    }

    @Test
    void rendersScrumPageWithEmptyForm() throws Exception {
        mockMvc.perform(get("/ui/scrum"))
                .andExpect(status().isOk())
                .andExpect(view().name("scrum/index"))
                .andExpect(model().attributeExists("scrumForm"))
                .andExpect(model().attribute("activePage", "scrum"));
    }

    @Test
    void createsScrumItemWithPostRedirectGet() throws Exception {
        mockMvc.perform(post("/ui/scrum")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Проверить API")
                        .param("description", "Подробное описание")
                        .param("module", "planner-core"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/scrum"))
                .andExpect(flash().attribute("successMessage", "Scrum-задача добавлена"));

        verify(service).createScrumItem("Проверить API", "Подробное описание", "planner-core");
    }

    @Test
    void keepsInvalidScrumFormAndDoesNotWrite() throws Exception {
        mockMvc.perform(post("/ui/scrum")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", " ")
                        .param("description", "")
                        .param("module", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("scrum/index"))
                .andExpect(model().attributeHasFieldErrors(
                        "scrumForm", "title", "description", "module"));

        verify(service, never()).createScrumItem(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private TaskListPageView emptyTaskList() {
        return new TaskListPageView(List.of(), "", "", List.of(), List.of());
    }

    private ScrumPageView emptyScrumPage() {
        return new ScrumPageView(List.of(), "", List.of());
    }

    private TaskDetailsPageView taskDetails() {
        return new TaskDetailsPageView(
                6L, "Анализ", "planner-core", "COMPLETED", "Завершена",
                "status-completed", "29.10.2026 10:00", "29.10.2026 11:00",
                "Постановка", "Результат", true, List.of(), List.of());
    }
}
