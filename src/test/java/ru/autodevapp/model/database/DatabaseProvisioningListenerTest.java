package ru.autodevapp.model.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.mock.env.MockEnvironment;
import ru.autodevapp.AutoDevAppApplication;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseProvisioningListenerTest {

    @Test
    void provisionsDatabaseFromPreparedEnvironment() {
        RecordingProvisioner provisioner = new RecordingProvisioner();
        DatabaseProvisioningListener listener = new DatabaseProvisioningListener(provisioner);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.database.admin-url", "jdbc:postgresql://postgres/postgres")
                .withProperty("app.database.admin-username", "admin")
                .withProperty("app.database.admin-password", "secret")
                .withProperty("app.database.name", "auto_dev_app");
        ApplicationEnvironmentPreparedEvent event = new ApplicationEnvironmentPreparedEvent(
                new DefaultBootstrapContext(),
                new SpringApplication(AutoDevAppApplication.class),
                new String[0],
                environment);

        listener.onApplicationEvent(event);

        assertThat(provisioner.settings).isEqualTo(new DatabaseSettings(
                "jdbc:postgresql://postgres/postgres", "admin", "secret", "auto_dev_app"));
    }

    static class RecordingProvisioner extends DatabaseProvisioner {

        DatabaseSettings settings;

        RecordingProvisioner() {
            super(ignored -> {
                throw new AssertionError("JDBC не должен вызываться в listener test");
            });
        }

        @Override
        public void provision(DatabaseSettings settings) {
            this.settings = settings;
        }
    }
}
