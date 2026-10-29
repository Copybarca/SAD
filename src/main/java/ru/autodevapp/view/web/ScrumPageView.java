package ru.autodevapp.view.web;

import java.util.List;

/**
 * Представляет форму и существующую очередь scrum-задач.
 */
public record ScrumPageView(
        List<ScrumItem> items,
        String moduleFilter,
        List<String> modules
) {

    public ScrumPageView {
        items = List.copyOf(items);
        modules = List.copyOf(modules);
    }

    public record ScrumItem(
            Long id,
            String title,
            String description,
            String module,
            String createdAt,
            String updatedAt
    ) {
    }
}
