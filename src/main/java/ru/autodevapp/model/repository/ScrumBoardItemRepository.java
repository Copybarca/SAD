package ru.autodevapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.autodevapp.model.entity.ScrumBoardItem;

import java.util.List;

/**
 * Предоставляет persistence-операции для элементов scrum-доски.
 */
public interface ScrumBoardItemRepository extends JpaRepository<ScrumBoardItem, Long> {

    List<ScrumBoardItem> findAllByOrderByCreatedAtDescIdDesc();

    List<ScrumBoardItem> findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(String module);
}
