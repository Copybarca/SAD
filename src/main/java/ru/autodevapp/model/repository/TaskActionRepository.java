package ru.autodevapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.autodevapp.model.entity.TaskAction;

import java.util.List;
import java.util.Optional;

/**
 * Хранит упорядоченный журнал действий по задаче.
 */
public interface TaskActionRepository extends JpaRepository<TaskAction, Long> {

    List<TaskAction> findAllByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    Optional<TaskAction> findByIdAndTaskId(Long id, Long taskId);
}
