package ru.autodevapp.view.web;

import java.util.List;

/**
 * Представляет отфильтрованный список задач агентов.
 */
public record TaskListPageView(
        List<TaskSummary> tasks,
        String moduleFilter,
        String statusFilter,
        List<String> modules,
        List<StatusOption> statuses
) {

    public TaskListPageView {
        tasks = List.copyOf(tasks);
        modules = List.copyOf(modules);
        statuses = List.copyOf(statuses);
    }

    public record TaskSummary(
            Long id,
            String title,
            String module,
            String statusCode,
            String statusLabel,
            String statusClass,
            String updatedAt
    ) {
    }

    public record StatusOption(String code, String label) {
    }
}
