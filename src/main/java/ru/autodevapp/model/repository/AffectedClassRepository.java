package ru.autodevapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.autodevapp.model.entity.AffectedClass;

import java.util.List;
import java.util.Optional;

/**
 * Хранит перечень классов, изменённых в рамках задачи.
 */
public interface AffectedClassRepository extends JpaRepository<AffectedClass, Long> {

    List<AffectedClass> findAllByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    Optional<AffectedClass> findByIdAndTaskId(Long id, Long taskId);

    boolean existsByTaskIdAndClassName(Long taskId, String className);

    boolean existsByTaskIdAndClassNameAndIdNot(Long taskId, String className, Long id);
}
