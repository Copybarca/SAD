package ru.autodevapp.model.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Предоставляет persistence-операции для основных задач агентов.
 */
public interface DevelopmentTaskRepository extends JpaRepository<DevelopmentTask, Long> {

    List<DevelopmentTask> findAllByOrderByCreatedAtDescIdDesc();

    List<DevelopmentTask> findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc(String module);

    List<DevelopmentTask> findAllByStatusOrderByCreatedAtDescIdDesc(TaskStatus status);

    List<DevelopmentTask> findAllByModuleIgnoreCaseAndStatusOrderByCreatedAtDescIdDesc(
            String module, TaskStatus status);

    @EntityGraph(attributePaths = {"affectedClasses", "actions"})
    @Query("select task from DevelopmentTask task where task.id = :id")
    Optional<DevelopmentTask> findDetailedById(@Param("id") Long id);
}
