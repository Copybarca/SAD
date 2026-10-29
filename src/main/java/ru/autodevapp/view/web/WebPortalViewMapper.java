package ru.autodevapp.view.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.WebDashboardData;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Преобразует domain data в immutable модели серверных HTML-представлений.
 */
@Component
public class WebPortalViewMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneOffset.UTC);
    private static final String EMPTY_RESULT = "Результат ещё не записан";

    public DashboardPageView toDashboardPage(WebDashboardData data) {
        return new DashboardPageView(
                data.totalTasks(),
                count(data, TaskStatus.PLANNED),
                count(data, TaskStatus.IN_PROGRESS),
                count(data, TaskStatus.COMPLETED),
                count(data, TaskStatus.FAILED),
                data.scrumCount(),
                data.recentTasks().stream().map(this::toTaskSummary).toList(),
                data.recentScrumItems().stream().map(this::toScrumItem).toList());
    }

    public TaskListPageView toTaskListPage(
            List<DevelopmentTask> tasks,
            List<DevelopmentTask> allTasks,
            String module,
            TaskStatus status) {
        List<String> moduleOptions = modules(
                allTasks.stream().map(DevelopmentTask::getModule).toList(),
                module);
        return new TaskListPageView(
                tasks.stream().map(this::toTaskSummary).toList(),
                selectedModule(moduleOptions, module),
                status == null ? "" : status.name(),
                moduleOptions,
                Arrays.stream(TaskStatus.values())
                        .map(value -> new TaskListPageView.StatusOption(value.name(), statusLabel(value)))
                        .toList());
    }

    public TaskDetailsPageView toTaskDetailsPage(DevelopmentTask task) {
        boolean hasResult = StringUtils.hasText(task.getResultDescription());
        return new TaskDetailsPageView(
                task.getId(),
                task.getTitle(),
                task.getModule(),
                task.getStatus().name(),
                statusLabel(task.getStatus()),
                statusClass(task.getStatus()),
                format(task.getCreatedAt()),
                format(task.getUpdatedAt()),
                task.getSpecification(),
                hasResult ? task.getResultDescription() : EMPTY_RESULT,
                hasResult,
                task.getAffectedClasses().stream()
                        .map(item -> new TaskDetailsPageView.AffectedClassItem(
                                item.getClassName(),
                                item.getChangeDescription(),
                                format(item.getCreatedAt())))
                        .toList(),
                task.getActions().stream()
                        .map(item -> new TaskDetailsPageView.ActionItem(
                                item.getActionDescription(),
                                item.getActionResult(),
                                format(item.getCreatedAt())))
                        .toList());
    }

    public ScrumPageView toScrumPage(
            List<ScrumBoardItem> items,
            List<ScrumBoardItem> allItems,
            String module) {
        List<String> moduleOptions = modules(
                allItems.stream().map(ScrumBoardItem::getModule).toList(),
                module);
        return new ScrumPageView(
                items.stream().map(this::toScrumItem).toList(),
                selectedModule(moduleOptions, module),
                moduleOptions);
    }

    private long count(WebDashboardData data, TaskStatus status) {
        return data.countsByStatus().getOrDefault(status, 0L);
    }

    private TaskListPageView.TaskSummary toTaskSummary(DevelopmentTask task) {
        return new TaskListPageView.TaskSummary(
                task.getId(),
                task.getTitle(),
                task.getModule(),
                task.getStatus().name(),
                statusLabel(task.getStatus()),
                statusClass(task.getStatus()),
                format(task.getUpdatedAt()));
    }

    private ScrumPageView.ScrumItem toScrumItem(ScrumBoardItem item) {
        return new ScrumPageView.ScrumItem(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getModule(),
                format(item.getCreatedAt()),
                format(item.getUpdatedAt()));
    }

    private List<String> modules(List<String> values, String requestedModule) {
        TreeMap<String, String> distinct = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        values.stream()
                .filter(StringUtils::hasText)
                .forEach(value -> distinct.putIfAbsent(value, value));
        if (StringUtils.hasText(requestedModule)) {
            distinct.putIfAbsent(requestedModule, requestedModule);
        }
        return List.copyOf(distinct.values());
    }

    private String selectedModule(List<String> availableModules, String requestedModule) {
        if (!StringUtils.hasText(requestedModule)) {
            return "";
        }
        return availableModules.stream()
                .filter(module -> module.equalsIgnoreCase(requestedModule))
                .findFirst()
                .orElse(requestedModule);
    }

    private String format(Instant value) {
        return value == null ? "—" : DATE_TIME_FORMATTER.format(value);
    }

    private String statusLabel(TaskStatus status) {
        return switch (status) {
            case PLANNED -> "Запланирована";
            case IN_PROGRESS -> "В работе";
            case COMPLETED -> "Завершена";
            case FAILED -> "Ошибка";
        };
    }

    private String statusClass(TaskStatus status) {
        return switch (status) {
            case PLANNED -> "status-planned";
            case IN_PROGRESS -> "status-in-progress";
            case COMPLETED -> "status-completed";
            case FAILED -> "status-failed";
        };
    }
}
