package ru.autodevapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранит постановку задачи, модуль, состояние и итог работы агента.
 */
@Entity
@Table(name = "development_task")
public class DevelopmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String specification;

    @Column(nullable = false, length = 255)
    private String module;

    @Column(name = "result_description", columnDefinition = "TEXT")
    private String resultDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "task")
    @OrderBy("createdAt ASC, id ASC")
    private List<AffectedClass> affectedClasses = new ArrayList<>();

    @OneToMany(mappedBy = "task")
    @OrderBy("createdAt ASC, id ASC")
    private List<TaskAction> actions = new ArrayList<>();

    protected DevelopmentTask() {
    }

    public DevelopmentTask(String title, String specification, String module) {
        this.title = title;
        this.specification = specification;
        this.module = module;
        this.status = TaskStatus.PLANNED;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = Instant.now();
    }

    public void update(String title, String specification, String module, TaskStatus status,
                       String resultDescription) {
        this.title = title;
        this.specification = specification;
        this.module = module;
        this.status = status;
        this.resultDescription = resultDescription;
    }

    public void updateResult(TaskStatus status, String resultDescription) {
        this.status = status;
        this.resultDescription = resultDescription;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSpecification() {
        return specification;
    }

    public String getModule() {
        return module;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<AffectedClass> getAffectedClasses() {
        return List.copyOf(affectedClasses);
    }

    public List<TaskAction> getActions() {
        return List.copyOf(actions);
    }
}
