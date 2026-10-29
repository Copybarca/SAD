package ru.autodevapp.model.web.repository;

import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Предоставляет минимальный набор данных для серверных HTML-представлений.
 */
public interface WebPortalRepository {

    List<DevelopmentTask> findTasks(String module, TaskStatus status);

    Optional<DevelopmentTask> findDetailedTask(Long taskId);

    List<ScrumBoardItem> findScrumItems(String module);

    ScrumBoardItem saveScrumItem(ScrumBoardItem item);
}
