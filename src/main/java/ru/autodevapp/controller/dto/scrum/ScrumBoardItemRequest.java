package ru.autodevapp.controller.dto.scrum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Принимает поля элемента простой scrum-доски.
 */
public record ScrumBoardItemRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @NotBlank @Size(max = 255) String module
) {
}
