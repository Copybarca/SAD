package ru.autodevapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Фиксирует класс, затронутый при выполнении конкретной задачи.
 */
@Entity
@Table(name = "affected_class", uniqueConstraints =
        @UniqueConstraint(name = "uk_affected_class_task_name", columnNames = {"task_id", "class_name"}))
public class AffectedClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private DevelopmentTask task;

    @Column(name = "class_name", nullable = false, length = 500)
    private String className;

    @Column(name = "change_description", nullable = false, columnDefinition = "TEXT")
    private String changeDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AffectedClass() {
    }

    public AffectedClass(DevelopmentTask task, String className, String changeDescription) {
        this.task = task;
        this.className = className;
        this.changeDescription = changeDescription;
    }

    @PrePersist
    void initializeCreatedAt() {
        createdAt = Instant.now();
    }

    public void update(String className, String changeDescription) {
        this.className = className;
        this.changeDescription = changeDescription;
    }

    public Long getId() {
        return id;
    }

    public DevelopmentTask getTask() {
        return task;
    }

    public String getClassName() {
        return className;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
