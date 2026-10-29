package ru.autodevapp.view;

import org.springframework.stereotype.Component;
import ru.autodevapp.controller.dto.scrum.ScrumBoardItemResponse;
import ru.autodevapp.model.entity.ScrumBoardItem;

/**
 * Преобразует элемент scrum-доски в представление API.
 */
@Component
public class ScrumBoardItemViewMapper {

    public ScrumBoardItemResponse toResponse(ScrumBoardItem item) {
        return new ScrumBoardItemResponse(
                item.getId(), item.getTitle(), item.getDescription(), item.getModule(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}
