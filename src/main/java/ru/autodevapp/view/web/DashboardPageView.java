package ru.autodevapp.view.web;

import java.util.List;

/**
 * Представляет данные обзорного HTML-экрана.
 */
public record DashboardPageView(
        long totalTasks,
        long plannedTasks,
        long inProgressTasks,
        long completedTasks,
        long failedTasks,
        long scrumCount,
        List<TaskListPageView.TaskSummary> recentTasks,
        List<ScrumPageView.ScrumItem> recentScrumItems
) {

    public DashboardPageView {
        recentTasks = List.copyOf(recentTasks);
        recentScrumItems = List.copyOf(recentScrumItems);
    }
}
