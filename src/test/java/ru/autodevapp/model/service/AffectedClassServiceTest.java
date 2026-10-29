package ru.autodevapp.model.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.autodevapp.exception.DuplicateResourceException;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.repository.AffectedClassRepository;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffectedClassServiceTest {

    @Mock
    AffectedClassRepository repository;

    @Mock
    DevelopmentTaskRepository taskRepository;

    @Test
    void createsClassOnlyForExistingTask() {
        DevelopmentTask task = new DevelopmentTask("Задача", "ТЗ", "planner");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.saveAndFlush(any(AffectedClass.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AffectedClassService service = new AffectedClassService(repository, taskRepository);

        AffectedClass created = service.create(1L, "TaskService", "Добавлен метод");

        assertThat(created.getClassName()).isEqualTo("TaskService");
    }

    @Test
    void duplicateClassIsRejected() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(
                new DevelopmentTask("Задача", "ТЗ", "planner")));
        when(repository.existsByTaskIdAndClassName(1L, "TaskService")).thenReturn(true);
        AffectedClassService service = new AffectedClassService(repository, taskRepository);

        assertThatThrownBy(() -> service.create(1L, "TaskService", "Повтор"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void classFromAnotherTaskIsNotVisible() {
        when(repository.findByIdAndTaskId(8L, 2L)).thenReturn(Optional.empty());
        AffectedClassService service = new AffectedClassService(repository, taskRepository);

        assertThatThrownBy(() -> service.update(2L, 8L, "Other", "Изменение"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void concurrentDuplicateInsertIsTranslatedToConflict() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(
                new DevelopmentTask("Задача", "ТЗ", "planner")));
        when(repository.saveAndFlush(any(AffectedClass.class)))
                .thenThrow(new DataIntegrityViolationException("uk_affected_class_task_name"));
        AffectedClassService service = new AffectedClassService(repository, taskRepository);

        assertThatThrownBy(() -> service.create(1L, "TaskService", "Гонка"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void concurrentDuplicateUpdateIsTranslatedToConflict() {
        AffectedClass affectedClass = new AffectedClass(
                new DevelopmentTask("Задача", "ТЗ", "planner"), "OldClass", "До");
        when(repository.findByIdAndTaskId(8L, 1L)).thenReturn(Optional.of(affectedClass));
        doThrow(new DataIntegrityViolationException("uk_affected_class_task_name"))
                .when(repository).flush();
        AffectedClassService service = new AffectedClassService(repository, taskRepository);

        assertThatThrownBy(() -> service.update(1L, 8L, "TaskService", "После"))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
