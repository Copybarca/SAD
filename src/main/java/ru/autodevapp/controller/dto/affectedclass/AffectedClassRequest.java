package ru.autodevapp.controller.dto.affectedclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Принимает имя затронутого класса и описание его изменения.
 */
public record AffectedClassRequest(
        @NotBlank @Size(max = 500) String className,
        @NotBlank String changeDescription
) {
}
