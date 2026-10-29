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

import java.time.Instant;

/**
 * Хранит краткое действие агента и полученный результат в рамках задачи.
 */
@Entity
@Table(name = "task_action")
public class TaskAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private DevelopmentTask task;

    @Column(name = "action_description", nullable = false, columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "action_result", columnDefinition = "TEXT")
    private String actionResult;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TaskAction() {
    }

    public TaskAction(DevelopmentTask task, String actionDescription, String actionResult) {
        this.task = task;
        this.actionDescription = actionDescription;
        this.actionResult = actionResult;
    }

    @PrePersist
    void initializeCreatedAt() {
        createdAt = Instant.now();
    }

    public void update(String actionDescription, String actionResult) {
        this.actionDescription = actionDescription;
        this.actionResult = actionResult;
    }

    public Long getId() {
        return id;
    }

    public DevelopmentTask getTask() {
        return task;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public String getActionResult() {
        return actionResult;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
