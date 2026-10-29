package ru.autodevapp.view;

import org.springframework.stereotype.Component;
import ru.autodevapp.controller.dto.affectedclass.AffectedClassResponse;
import ru.autodevapp.model.entity.AffectedClass;

/**
 * Преобразует запись затронутого класса в представление API.
 */
@Component
public class AffectedClassViewMapper {

    public AffectedClassResponse toResponse(AffectedClass affectedClass) {
        return new AffectedClassResponse(
                affectedClass.getId(),
                affectedClass.getClassName(),
                affectedClass.getChangeDescription(),
                affectedClass.getCreatedAt());
    }
}
