package ru.autodevapp.model.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.TaskAction;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;
import ru.autodevapp.model.repository.TaskActionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskActionServiceTest {

    @Mock
    TaskActionRepository repository;

    @Mock
    DevelopmentTaskRepository taskRepository;

    @Test
    void returnsRepositoryHistoryOrder() {
        TaskAction first = new TaskAction(null, "Первое", null);
        TaskAction second = new TaskAction(null, "Второе", "Готово");
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(repository.findAllByTaskIdOrderByCreatedAtAscIdAsc(1L))
                .thenReturn(List.of(first, second));
        TaskActionService service = new TaskActionService(repository, taskRepository);

        assertThat(service.findAll(1L)).extracting(TaskAction::getActionDescription)
                .containsExactly("Первое", "Второе");
    }

    @Test
    void actionFromAnotherTaskIsNotVisible() {
        when(repository.findByIdAndTaskId(9L, 2L)).thenReturn(Optional.empty());
        TaskActionService service = new TaskActionService(repository, taskRepository);

        assertThatThrownBy(() -> service.delete(2L, 9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
