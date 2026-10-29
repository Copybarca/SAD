package ru.autodevapp.view.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Принимает единственную разрешённую из HTML mutation — новую scrum-задачу.
 */
public class ScrumItemForm {

    @NotBlank(message = "Укажите название")
    @Size(max = 255, message = "Название должно быть не длиннее 255 символов")
    private String title;

    @NotBlank(message = "Опишите задачу")
    private String description;

    @NotBlank(message = "Укажите модуль")
    @Size(max = 255, message = "Модуль должен быть не длиннее 255 символов")
    private String module;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
