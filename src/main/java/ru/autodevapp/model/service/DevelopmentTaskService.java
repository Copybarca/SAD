package ru.autodevapp.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;

import java.util.List;

/**
 * Выполняет сценарии создания, поиска, изменения и удаления основных задач.
 */
@Service
public class DevelopmentTaskService {

    private final DevelopmentTaskRepository repository;

    public DevelopmentTaskService(DevelopmentTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DevelopmentTask create(String title, String specification, String module) {
        return repository.save(new DevelopmentTask(title, specification, module));
    }

    @Transactional(readOnly = true)
    public DevelopmentTask findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<DevelopmentTask> findAll(String module, TaskStatus status) {
        boolean hasModule = StringUtils.hasText(module);
        if (hasModule && status != null) {
            return repository.findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(module, status);
        }
        if (hasModule) {
            return repository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(module);
        }
        if (status != null) {
            return repository.findAllByStatusOrderByCreatedAtDescIdDesc(status);
        }
        return repository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public DevelopmentTask getHistory(Long id) {
        DevelopmentTask task = findById(id);
        task.getAffectedClasses().size();
        task.getActions().size();
        return task;
    }

    @Transactional
    public DevelopmentTask update(Long id, String title, String specification, String module,
                                  TaskStatus status, String resultDescription) {
        validateResult(status, resultDescription);
        DevelopmentTask task = findById(id);
        task.update(title, specification, module, status, resultDescription);
        return task;
    }

    @Transactional
    public DevelopmentTask updateResult(Long id, TaskStatus status, String resultDescription) {
        validateResult(status, resultDescription);
        DevelopmentTask task = findById(id);
        task.updateResult(status, resultDescription);
        return task;
    }

    @Transactional
    public void delete(Long id) {
        DevelopmentTask task = findById(id);
        repository.delete(task);
    }

    private void validateResult(TaskStatus status, String resultDescription) {
        if ((status == TaskStatus.COMPLETED || status == TaskStatus.FAILED)
                && !StringUtils.hasText(resultDescription)) {
            throw new IllegalArgumentException("Для завершённой задачи требуется описание результата");
        }
    }
}
