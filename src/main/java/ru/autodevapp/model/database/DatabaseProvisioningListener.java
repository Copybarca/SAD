package ru.autodevapp.model.database;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Создаёт целевую БД после загрузки properties, но до инициализации datasource.
 */
public class DatabaseProvisioningListener
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final DatabaseProvisioner provisioner;

    public DatabaseProvisioningListener() {
        this(new DatabaseProvisioner(new DriverManagerAdminConnectionFactory()));
    }

    DatabaseProvisioningListener(DatabaseProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        provisioner.provision(DatabaseSettings.from(event.getEnvironment()));
    }
}
