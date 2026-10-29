package ru.autodevapp.model.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentTaskServiceTest {

    @Mock
    DevelopmentTaskRepository repository;

    @Test
    void newTaskStartsAsPlanned() {
        when(repository.save(org.mockito.ArgumentMatchers.any(DevelopmentTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DevelopmentTaskService service = new DevelopmentTaskService(repository);

        DevelopmentTask created = service.create("Заголовок", "Техническое задание", "planner");

        assertThat(created.getStatus()).isEqualTo(TaskStatus.PLANNED);
        assertThat(created.getTitle()).isEqualTo("Заголовок");
        assertThat(created.getModule()).isEqualTo("planner");
    }

    @Test
    void listUsesBothFiltersWhenProvided() {
        when(repository.findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(
                "planner", TaskStatus.IN_PROGRESS)).thenReturn(List.of());
        DevelopmentTaskService service = new DevelopmentTaskService(repository);

        service.findAll("planner", TaskStatus.IN_PROGRESS);

        verify(repository).findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(
                "planner", TaskStatus.IN_PROGRESS);
    }

    @Test
    void completedTaskRequiresResultDescription() {
        DevelopmentTaskService service = new DevelopmentTaskService(repository);

        assertThatThrownBy(() -> service.updateResult(7L, TaskStatus.COMPLETED, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("результат");
    }

    @Test
    void missingTaskProducesDomainNotFoundError() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        DevelopmentTaskService service = new DevelopmentTaskService(repository);

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
