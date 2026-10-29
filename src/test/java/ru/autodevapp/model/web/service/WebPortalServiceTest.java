package ru.autodevapp.model.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.WebDashboardData;
import ru.autodevapp.model.web.repository.WebPortalRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebPortalServiceTest {

    @Mock
    WebPortalRepository repository;

    @Test
    void buildsDashboardCountsAndLimitsRecentLists() {
        List<DevelopmentTask> tasks = List.of(
                task("T1", TaskStatus.COMPLETED),
                task("T2", TaskStatus.IN_PROGRESS),
                task("T3", TaskStatus.PLANNED),
                task("T4", TaskStatus.FAILED),
                task("T5", TaskStatus.COMPLETED),
                task("T6", TaskStatus.PLANNED));
        List<ScrumBoardItem> scrumItems = List.of(
                scrum("S1"), scrum("S2"), scrum("S3"),
                scrum("S4"), scrum("S5"), scrum("S6"));
        when(repository.findTasks(null, null)).thenReturn(tasks);
        when(repository.findScrumItems(null)).thenReturn(scrumItems);

        WebDashboardData dashboard = new WebPortalService(repository).loadDashboard();

        assertThat(dashboard.totalTasks()).isEqualTo(6);
        assertThat(dashboard.countsByStatus()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                TaskStatus.PLANNED, 2L,
                TaskStatus.IN_PROGRESS, 1L,
                TaskStatus.COMPLETED, 2L,
                TaskStatus.FAILED, 1L));
        assertThat(dashboard.recentTasks()).extracting(DevelopmentTask::getTitle)
                .containsExactly("T1", "T2", "T3", "T4", "T5");
        assertThat(dashboard.scrumCount()).isEqualTo(6);
        assertThat(dashboard.recentScrumItems()).extracting(ScrumBoardItem::getTitle)
                .containsExactly("S1", "S2", "S3", "S4", "S5");
    }

    @Test
    void preservesTaskFilters() {
        List<DevelopmentTask> expected = List.of(task("Planner", TaskStatus.IN_PROGRESS));
        when(repository.findTasks("planner-core", TaskStatus.IN_PROGRESS)).thenReturn(expected);

        List<DevelopmentTask> actual = new WebPortalService(repository)
                .findTasks("planner-core", TaskStatus.IN_PROGRESS);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void rejectsUnknownTask() {
        when(repository.findDetailedTask(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new WebPortalService(repository).findTask(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Задача 42 не найдена");
    }

    @Test
    void createsOnlyScrumEntityThroughRepository() {
        when(repository.saveScrumItem(any(ScrumBoardItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScrumBoardItem created = new WebPortalService(repository)
                .createScrumItem("Проверить план", "Подробное описание", "planner-core");

        assertThat(created.getTitle()).isEqualTo("Проверить план");
        assertThat(created.getDescription()).isEqualTo("Подробное описание");
        assertThat(created.getModule()).isEqualTo("planner-core");
        ArgumentCaptor<ScrumBoardItem> captor = ArgumentCaptor.forClass(ScrumBoardItem.class);
        verify(repository).saveScrumItem(captor.capture());
        assertThat(captor.getValue()).isSameAs(created);
    }

    private DevelopmentTask task(String title, TaskStatus status) {
        DevelopmentTask task = new DevelopmentTask(title, "Specification", "planner-core");
        task.update(title, "Specification", "planner-core", status,
                status == TaskStatus.COMPLETED || status == TaskStatus.FAILED ? "Result" : null);
        return task;
    }

    private ScrumBoardItem scrum(String title) {
        return new ScrumBoardItem(title, "Description", "planner-core");
    }
}
