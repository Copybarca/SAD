package ru.autodevapp.view.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.WebDashboardData;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebPortalViewMapperTest {

    private final WebPortalViewMapper mapper = new WebPortalViewMapper();

    @Test
    void mapsDashboardCountsAndRecentItems() {
        DevelopmentTask task = task(7L, "Анализ", "planner-core", TaskStatus.IN_PROGRESS);
        ScrumBoardItem scrum = scrum(3L, "Новая задача", "data-core");
        WebDashboardData data = new WebDashboardData(
                4,
                Map.of(
                        TaskStatus.PLANNED, 1L,
                        TaskStatus.IN_PROGRESS, 1L,
                        TaskStatus.COMPLETED, 2L,
                        TaskStatus.FAILED, 0L),
                List.of(task),
                1,
                List.of(scrum));

        DashboardPageView view = mapper.toDashboardPage(data);

        assertThat(view.totalTasks()).isEqualTo(4);
        assertThat(view.inProgressTasks()).isEqualTo(1);
        assertThat(view.recentTasks()).singleElement().satisfies(item -> {
            assertThat(item.statusLabel()).isEqualTo("В работе");
            assertThat(item.statusClass()).isEqualTo("status-in-progress");
            assertThat(item.updatedAt()).isEqualTo("29.10.2026 12:15");
        });
        assertThat(view.recentScrumItems()).singleElement()
                .extracting(ScrumPageView.ScrumItem::module)
                .isEqualTo("data-core");
    }

    @Test
    void mapsTaskDetailsWithNullResultAndEmptyChildren() {
        DevelopmentTask task = task(9L, "Без результата", "planner-core", TaskStatus.PLANNED);

        TaskDetailsPageView view = mapper.toTaskDetailsPage(task);

        assertThat(view.resultDescription()).isEqualTo("Результат ещё не записан");
        assertThat(view.hasResult()).isFalse();
        assertThat(view.affectedClasses()).isEmpty();
        assertThat(view.actions()).isEmpty();
        assertThat(view.statusLabel()).isEqualTo("Запланирована");
    }

    @Test
    void mapsDetailedChildrenAndPreservesLongText() {
        DevelopmentTask task = task(10L, "Готово", "planner-core", TaskStatus.COMPLETED);
        task.update("Готово", "Строка 1\nСтрока 2", "planner-core", TaskStatus.COMPLETED,
                "Итог 1\nИтог 2");
        AffectedClass affectedClass = new AffectedClass(task, "ru.example.Service", "Изменена логика");
        TaskAction action = new TaskAction(task, "Проверен код", "Причина найдена");
        ReflectionTestUtils.setField(affectedClass, "createdAt", Instant.parse("2026-10-29T10:00:00Z"));
        ReflectionTestUtils.setField(action, "createdAt", Instant.parse("2026-10-29T10:05:00Z"));
        ReflectionTestUtils.setField(task, "affectedClasses", new java.util.ArrayList<>(List.of(affectedClass)));
        ReflectionTestUtils.setField(task, "actions", new java.util.ArrayList<>(List.of(action)));

        TaskDetailsPageView view = mapper.toTaskDetailsPage(task);

        assertThat(view.specification()).isEqualTo("Строка 1\nСтрока 2");
        assertThat(view.resultDescription()).isEqualTo("Итог 1\nИтог 2");
        assertThat(view.hasResult()).isTrue();
        assertThat(view.affectedClasses()).singleElement()
                .extracting(TaskDetailsPageView.AffectedClassItem::className)
                .isEqualTo("ru.example.Service");
        assertThat(view.actions()).singleElement()
                .extracting(TaskDetailsPageView.ActionItem::result)
                .isEqualTo("Причина найдена");
    }

    @Test
    void buildsSortedDistinctTaskFilters() {
        List<DevelopmentTask> filtered = List.of(task(1L, "A", "planner-core", TaskStatus.FAILED));
        List<DevelopmentTask> all = List.of(
                task(1L, "A", "planner-core", TaskStatus.FAILED),
                task(2L, "B", "Data-Core", TaskStatus.PLANNED),
                task(3L, "C", "data-core", TaskStatus.COMPLETED));

        TaskListPageView view = mapper.toTaskListPage(
                filtered, all, "planner-core", TaskStatus.FAILED);

        assertThat(view.moduleFilter()).isEqualTo("planner-core");
        assertThat(view.statusFilter()).isEqualTo("FAILED");
        assertThat(view.modules()).containsExactly("Data-Core", "planner-core");
        assertThat(view.statuses()).extracting(TaskListPageView.StatusOption::code)
                .containsExactly("PLANNED", "IN_PROGRESS", "COMPLETED", "FAILED");
    }

    @Test
    void mapsScrumFilterAndModules() {
        List<ScrumBoardItem> filtered = List.of(scrum(1L, "A", "planner-core"));
        List<ScrumBoardItem> all = List.of(
                scrum(1L, "A", "planner-core"),
                scrum(2L, "B", "users"));

        ScrumPageView view = mapper.toScrumPage(filtered, all, "planner-core");

        assertThat(view.moduleFilter()).isEqualTo("planner-core");
        assertThat(view.modules()).containsExactly("planner-core", "users");
        assertThat(view.items()).singleElement()
                .extracting(ScrumPageView.ScrumItem::title)
                .isEqualTo("A");
    }

    @Test
    void selectsCanonicalModuleIgnoringRequestedCase() {
        List<DevelopmentTask> tasks = List.of(
                task(1L, "A", "planner-core", TaskStatus.COMPLETED));

        TaskListPageView view = mapper.toTaskListPage(
                tasks, tasks, "PLANNER-CORE", TaskStatus.COMPLETED);

        assertThat(view.moduleFilter()).isEqualTo("planner-core");
        assertThat(view.modules()).containsExactly("planner-core");
    }

    @Test
    void retainsRequestedScrumModuleWhenItIsAbsentFromCurrentRows() {
        List<ScrumBoardItem> all = List.of(scrum(1L, "A", "planner-core"));

        ScrumPageView view = mapper.toScrumPage(List.of(), all, "legacy-module");

        assertThat(view.moduleFilter()).isEqualTo("legacy-module");
        assertThat(view.modules()).containsExactly("legacy-module", "planner-core");
    }

    private DevelopmentTask task(Long id, String title, String module, TaskStatus status) {
        DevelopmentTask task = new DevelopmentTask(title, "Specification", module);
        task.update(title, "Specification", module, status,
                status == TaskStatus.COMPLETED || status == TaskStatus.FAILED ? "Result" : null);
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", Instant.parse("2026-10-29T11:00:00Z"));
        ReflectionTestUtils.setField(task, "updatedAt", Instant.parse("2026-10-29T12:15:00Z"));
        return task;
    }

    private ScrumBoardItem scrum(Long id, String title, String module) {
        ScrumBoardItem item = new ScrumBoardItem(title, "Description", module);
        ReflectionTestUtils.setField(item, "id", id);
        ReflectionTestUtils.setField(item, "createdAt", Instant.parse("2026-10-29T09:00:00Z"));
        ReflectionTestUtils.setField(item, "updatedAt", Instant.parse("2026-10-29T09:30:00Z"));
        return item;
    }
}
