package ru.autodevapp.model.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.WebDashboardData;
import ru.autodevapp.model.web.repository.WebPortalRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Выполняет use cases серверного HTML-портала.
 */
@Service
public class WebPortalService {

    private static final int RECENT_ITEM_LIMIT = 5;

    private final WebPortalRepository repository;

    public WebPortalService(WebPortalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WebDashboardData loadDashboard() {
        List<DevelopmentTask> tasks = repository.findTasks(null, null);
        List<ScrumBoardItem> scrumItems = repository.findScrumItems(null);
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, 0L);
        }
        tasks.forEach(task -> counts.compute(task.getStatus(), (status, count) -> count + 1));
        return new WebDashboardData(
                tasks.size(),
                counts,
                tasks.stream().limit(RECENT_ITEM_LIMIT).toList(),
                scrumItems.size(),
                scrumItems.stream().limit(RECENT_ITEM_LIMIT).toList());
    }

    @Transactional(readOnly = true)
    public List<DevelopmentTask> findTasks(String module, TaskStatus status) {
        return repository.findTasks(module, status);
    }

    @Transactional(readOnly = true)
    public DevelopmentTask findTask(Long taskId) {
        DevelopmentTask task = repository.findDetailedTask(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача " + taskId + " не найдена"));
        task.getAffectedClasses().size();
        task.getActions().size();
        return task;
    }

    @Transactional(readOnly = true)
    public List<ScrumBoardItem> findScrumItems(String module) {
        return repository.findScrumItems(module);
    }

    @Transactional
    public ScrumBoardItem createScrumItem(String title, String description, String module) {
        return repository.saveScrumItem(new ScrumBoardItem(title, description, module));
    }
}
