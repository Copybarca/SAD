package ru.autodevapp.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import ru.autodevapp.exception.DuplicateResourceException;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.repository.AffectedClassRepository;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;

import java.util.List;

/**
 * Управляет перечнем классов, затронутых конкретной задачей.
 */
@Service
public class AffectedClassService {

    private final AffectedClassRepository repository;
    private final DevelopmentTaskRepository taskRepository;

    public AffectedClassService(
            AffectedClassRepository repository, DevelopmentTaskRepository taskRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public AffectedClass create(Long taskId, String className, String changeDescription) {
        DevelopmentTask task = findTask(taskId);
        if (repository.existsByTaskIdAndClassName(taskId, className)) {
            throw duplicate(taskId, className);
        }
        try {
            return repository.saveAndFlush(new AffectedClass(task, className, changeDescription));
        } catch (DataIntegrityViolationException exception) {
            throw translateDuplicate(exception, taskId, className);
        }
    }

    @Transactional(readOnly = true)
    public List<AffectedClass> findAll(Long taskId) {
        ensureTaskExists(taskId);
        return repository.findAllByTaskIdOrderByCreatedAtAscIdAsc(taskId);
    }

    @Transactional
    public AffectedClass update(
            Long taskId, Long classId, String className, String changeDescription) {
        AffectedClass affectedClass = find(taskId, classId);
        if (repository.existsByTaskIdAndClassNameAndIdNot(taskId, className, classId)) {
            throw duplicate(taskId, className);
        }
        affectedClass.update(className, changeDescription);
        try {
            repository.flush();
            return affectedClass;
        } catch (DataIntegrityViolationException exception) {
            throw translateDuplicate(exception, taskId, className);
        }
    }

    @Transactional
    public void delete(Long taskId, Long classId) {
        repository.delete(find(taskId, classId));
    }

    private DevelopmentTask findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача " + taskId + " не найдена"));
    }

    private void ensureTaskExists(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Задача " + taskId + " не найдена");
        }
    }

    private AffectedClass find(Long taskId, Long classId) {
        return repository.findByIdAndTaskId(classId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Затронутый класс " + classId + " задачи " + taskId + " не найден"));
    }

    private DuplicateResourceException duplicate(Long taskId, String className) {
        return new DuplicateResourceException(
                "Класс " + className + " уже добавлен к задаче " + taskId);
    }

    private DuplicateResourceException translateDuplicate(
            DataIntegrityViolationException exception, Long taskId, String className) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null
                    && current.getMessage().contains("uk_affected_class_task_name")) {
                return duplicate(taskId, className);
            }
            current = current.getCause();
        }
        throw exception;
    }
}
