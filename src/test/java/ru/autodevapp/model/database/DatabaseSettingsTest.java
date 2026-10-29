package ru.autodevapp.model.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseSettingsTest {

    @Test
    void readsDatabaseAdministrationProperties() {
        MockEnvironment environment = validEnvironment();

        DatabaseSettings settings = DatabaseSettings.from(environment);

        assertThat(settings.adminUrl()).isEqualTo("jdbc:postgresql://postgres:5432/postgres");
        assertThat(settings.adminUsername()).isEqualTo("admin");
        assertThat(settings.adminPassword()).isEqualTo("secret");
        assertThat(settings.databaseName()).isEqualTo("auto_dev_app");
    }

    @Test
    void missingRequiredPropertyStopsStartup() {
        MockEnvironment environment = validEnvironment()
                .withProperty("app.database.admin-url", "");

        assertThatThrownBy(() -> DatabaseSettings.from(environment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("app.database.admin-url");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1database", "auto-dev-app", "db;drop database postgres", "имя_бд"})
    void unsafeDatabaseNameIsRejectedBeforeJdbc(String databaseName) {
        MockEnvironment environment = validEnvironment()
                .withProperty("app.database.name", databaseName);

        assertThatThrownBy(() -> DatabaseSettings.from(environment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("app.database.name");
    }

    @Test
    void directConstructionCannotBypassDatabaseNameValidation() {
        assertThatThrownBy(() -> new DatabaseSettings(
                "jdbc:postgresql://localhost/postgres", "admin", "secret", "db;drop"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("databaseName");
    }

    private MockEnvironment validEnvironment() {
        return new MockEnvironment()
                .withProperty("app.database.admin-url", "jdbc:postgresql://postgres:5432/postgres")
                .withProperty("app.database.admin-username", "admin")
                .withProperty("app.database.admin-password", "secret")
                .withProperty("app.database.name", "auto_dev_app");
    }
}
