package ru.autodevapp.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.repository.ScrumBoardItemRepository;

import java.util.List;

/**
 * Выполняет CRUD-сценарии простой scrum-доски.
 */
@Service
public class ScrumBoardItemService {

    private final ScrumBoardItemRepository repository;

    public ScrumBoardItemService(ScrumBoardItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ScrumBoardItem create(String title, String description, String module) {
        return repository.save(new ScrumBoardItem(title, description, module));
    }

    @Transactional(readOnly = true)
    public List<ScrumBoardItem> findAll(String module) {
        if (StringUtils.hasText(module)) {
            return repository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(module);
        }
        return repository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public ScrumBoardItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Элемент scrum-доски " + id + " не найден"));
    }

    @Transactional
    public ScrumBoardItem update(Long id, String title, String description, String module) {
        ScrumBoardItem item = findById(id);
        item.update(title, description, module);
        return item;
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
