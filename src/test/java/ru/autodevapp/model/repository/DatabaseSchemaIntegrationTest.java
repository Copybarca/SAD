package ru.autodevapp.model.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ru.autodevapp.model.entity.AffectedClass;
import ru.autodevapp.model.entity.DevelopmentTask;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.entity.TaskAction;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class DatabaseSchemaIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("auto_dev_app_test")
            .withUsername("test")
            .withPassword("test")
            .withNetworkMode(System.getenv().getOrDefault("TEST_DOCKER_NETWORK", "bridge"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DatabaseSchemaIntegrationTest::jdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    static String jdbcUrl() {
        String networkName = System.getenv("TEST_DOCKER_NETWORK");
        if (networkName == null || networkName.isBlank()) {
            return POSTGRES.getJdbcUrl();
        }
        String networkHost = POSTGRES.getContainerInfo().getNetworkSettings()
                .getNetworks().get(networkName).getIpAddress();
        return "jdbc:postgresql://" + networkHost + ":5432/" + POSTGRES.getDatabaseName();
    }

    @Autowired
    DevelopmentTaskRepository taskRepository;

    @Autowired
    AffectedClassRepository affectedClassRepository;

    @Autowired
    TaskActionRepository actionRepository;

    @Autowired
    ScrumBoardItemRepository scrumRepository;

    @Test
    void liquibaseSchemaPersistsEveryModel() {
        DevelopmentTask task = taskRepository.saveAndFlush(
                new DevelopmentTask("API истории", "Сохранить ход выполнения", "planner"));
        affectedClassRepository.saveAndFlush(
                new AffectedClass(task, "ru.example.TaskService", "Добавлен журнал"));
        actionRepository.saveAndFlush(
                new TaskAction(task, "Создана миграция", "Таблицы доступны"));
        scrumRepository.saveAndFlush(
                new ScrumBoardItem("API истории", "Добавить endpoints", "planner"));

        assertThat(taskRepository.findById(task.getId())).isPresent();
        assertThat(affectedClassRepository.findAllByTaskIdOrderByCreatedAtAscIdAsc(task.getId()))
                .extracting(AffectedClass::getClassName)
                .containsExactly("ru.example.TaskService");
        assertThat(actionRepository.findAllByTaskIdOrderByCreatedAtAscIdAsc(task.getId()))
                .extracting(TaskAction::getActionDescription)
                .containsExactly("Создана миграция");
        assertThat(scrumRepository.findAllByOrderByCreatedAtDescIdDesc())
                .extracting(ScrumBoardItem::getModule)
                .contains("planner");
    }

    @Test
    void sameClassCannotBeAddedTwiceToOneTask() {
        DevelopmentTask task = taskRepository.saveAndFlush(
                new DevelopmentTask("Уникальность", "Не дублировать класс", "data"));
        affectedClassRepository.saveAndFlush(new AffectedClass(task, "TaskService", "Первое изменение"));

        assertThatThrownBy(() -> affectedClassRepository.saveAndFlush(
                new AffectedClass(task, "TaskService", "Повторное изменение")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingTaskCascadesToItsDetails() {
        DevelopmentTask task = taskRepository.saveAndFlush(
                new DevelopmentTask("Удаление", "Удалить детали", "users"));
        affectedClassRepository.saveAndFlush(new AffectedClass(task, "UserService", "Изменён"));
        actionRepository.saveAndFlush(new TaskAction(task, "Удаление", null));

        taskRepository.deleteById(task.getId());
        taskRepository.flush();

        assertThat(affectedClassRepository.findAllByTaskIdOrderByCreatedAtAscIdAsc(task.getId())).isEmpty();
        assertThat(actionRepository.findAllByTaskIdOrderByCreatedAtAscIdAsc(task.getId())).isEmpty();
    }

    @Test
    void actionsAreReadInStableChronologicalOrder() {
        DevelopmentTask task = taskRepository.saveAndFlush(
                new DevelopmentTask("Порядок", "Вернуть историю", "eam"));
        TaskAction first = actionRepository.saveAndFlush(new TaskAction(task, "Первое", null));
        TaskAction second = actionRepository.saveAndFlush(new TaskAction(task, "Второе", "Готово"));

        List<TaskAction> actions = actionRepository.findAllByTaskIdOrderByCreatedAtAscIdAsc(task.getId());

        assertThat(actions).extracting(TaskAction::getId)
                .containsExactly(first.getId(), second.getId());
    }
}
