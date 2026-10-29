package ru.autodevapp.model.web.repository;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.repository.DevelopmentTaskRepository;
import ru.autodevapp.model.repository.ScrumBoardItemRepository;

import java.util.List;
import java.util.Optional;

/**
 * Адаптирует существующие Spring Data repositories к read-модели web-портала.
 */
@Repository
public class JpaWebPortalRepository implements WebPortalRepository {

    private final DevelopmentTaskRepository taskRepository;
    private final ScrumBoardItemRepository scrumRepository;

    public JpaWebPortalRepository(
            DevelopmentTaskRepository taskRepository,
            ScrumBoardItemRepository scrumRepository) {
        this.taskRepository = taskRepository;
        this.scrumRepository = scrumRepository;
    }

    @Override
    public List<DevelopmentTask> findTasks(String module, TaskStatus status) {
        boolean hasModule = StringUtils.hasText(module);
        if (hasModule && status != null) {
            return taskRepository.findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(
                    module, status);
        }
        if (hasModule) {
            return taskRepository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(module);
        }
        if (status != null) {
            return taskRepository.findAllByStatusOrderByCreatedAtDescIdDesc(status);
        }
        return taskRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Override
    public Optional<DevelopmentTask> findDetailedTask(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public List<ScrumBoardItem> findScrumItems(String module) {
        if (StringUtils.hasText(module)) {
            return scrumRepository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(module);
        }
        return scrumRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Override
    public ScrumBoardItem saveScrumItem(ScrumBoardItem item) {
        return scrumRepository.save(item);
    }
}
