package ru.autodevapp.view;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.autodevapp.controller.dto.task.TaskResponse;
import ru.autodevapp.model.entity.DevelopmentTask;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentTaskViewMapperTest {

    @Test
    void mapsTaskToStableApiRepresentation() {
        DevelopmentTask task = new DevelopmentTask("API", "Создать API", "planner");
        ReflectionTestUtils.setField(task, "id", 15L);
        ReflectionTestUtils.setField(task, "createdAt", Instant.parse("2026-10-28T07:00:00Z"));
        ReflectionTestUtils.setField(task, "updatedAt", Instant.parse("2026-10-28T07:05:00Z"));

        TaskResponse response = new DevelopmentTaskViewMapper().toResponse(task);

        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.title()).isEqualTo("API");
        assertThat(response.specification()).isEqualTo("Создать API");
        assertThat(response.module()).isEqualTo("planner");
        assertThat(response.status()).isEqualTo("PLANNED");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-28T07:00:00Z"));
    }
}
