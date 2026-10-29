package ru.autodevapp.model.web.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;
import ru.autodevapp.model.repository.ScrumBoardItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaWebPortalRepositoryTest {

    @Mock
    DevelopmentTaskRepository taskRepository;

    @Mock
    ScrumBoardItemRepository scrumRepository;

    JpaWebPortalRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaWebPortalRepository(taskRepository, scrumRepository);
    }

    @Test
    void usesCombinedTaskFilter() {
        List<DevelopmentTask> expected = List.of(task());
        when(taskRepository.findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(
                "planner", TaskStatus.COMPLETED)).thenReturn(expected);

        assertThat(repository.findTasks("planner", TaskStatus.COMPLETED)).isEqualTo(expected);
    }

    @Test
    void usesModuleOnlyTaskFilter() {
        List<DevelopmentTask> expected = List.of(task());
        when(taskRepository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc("planner"))
                .thenReturn(expected);

        assertThat(repository.findTasks("planner", null)).isEqualTo(expected);
    }

    @Test
    void usesStatusOnlyTaskFilter() {
        List<DevelopmentTask> expected = List.of(task());
        when(taskRepository.findAllByStatusOrderByCreatedAtDescIdDesc(TaskStatus.PLANNED))
                .thenReturn(expected);

        assertThat(repository.findTasks(" ", TaskStatus.PLANNED)).isEqualTo(expected);
    }

    @Test
    void loadsAllTasksWithoutFilters() {
        List<DevelopmentTask> expected = List.of(task());
        when(taskRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(expected);

        assertThat(repository.findTasks(null, null)).isEqualTo(expected);
    }

    @Test
    void loadsDetailedTaskWithoutJoiningTwoBagCollections() {
        DevelopmentTask expected = task();
        when(taskRepository.findById(7L)).thenReturn(Optional.of(expected));

        assertThat(repository.findDetailedTask(7L)).containsSame(expected);
        verify(taskRepository).findById(7L);
        verify(taskRepository, org.mockito.Mockito.never()).findDetailedById(7L);
    }

    @Test
    void filtersScrumItemsByModule() {
        List<ScrumBoardItem> expected = List.of(scrum());
        when(scrumRepository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc("planner"))
                .thenReturn(expected);

        assertThat(repository.findScrumItems("planner")).isEqualTo(expected);
    }

    @Test
    void loadsAllScrumItemsForBlankModule() {
        List<ScrumBoardItem> expected = List.of(scrum());
        when(scrumRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(expected);

        assertThat(repository.findScrumItems(" ")).isEqualTo(expected);
    }

    @Test
    void savesScrumItem() {
        ScrumBoardItem item = scrum();
        when(scrumRepository.save(item)).thenReturn(item);

        assertThat(repository.saveScrumItem(item)).isSameAs(item);
        verify(scrumRepository).save(item);
    }

    private DevelopmentTask task() {
        return new DevelopmentTask("Task", "Specification", "planner");
    }

    private ScrumBoardItem scrum() {
        return new ScrumBoardItem("Scrum", "Description", "planner");
    }
}
