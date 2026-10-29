package ru.autodevapp.model.web;

import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;

import java.util.List;
import java.util.Map;

/**
 * Содержит агрегированные данные главного HTML-экрана.
 */
public record WebDashboardData(
        long totalTasks,
        Map<TaskStatus, Long> countsByStatus,
        List<DevelopmentTask> recentTasks,
        long scrumCount,
        List<ScrumBoardItem> recentScrumItems
) {

    public WebDashboardData {
        countsByStatus = Map.copyOf(countsByStatus);
        recentTasks = List.copyOf(recentTasks);
        recentScrumItems = List.copyOf(recentScrumItems);
    }
}
