package ru.autodevapp.view.web;

import java.util.List;

/**
 * Представляет read-only подробности задачи агента.
 */
public record TaskDetailsPageView(
        Long id,
        String title,
        String module,
        String statusCode,
        String statusLabel,
        String statusClass,
        String createdAt,
        String updatedAt,
        String specification,
        String resultDescription,
        boolean hasResult,
        List<AffectedClassItem> affectedClasses,
        List<ActionItem> actions
) {

    public TaskDetailsPageView {
        affectedClasses = List.copyOf(affectedClasses);
        actions = List.copyOf(actions);
    }

    public record AffectedClassItem(String className, String description, String createdAt) {
    }

    public record ActionItem(String description, String result, String createdAt) {
    }
}
