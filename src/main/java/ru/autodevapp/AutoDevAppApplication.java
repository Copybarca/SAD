package ru.autodevapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.autodevapp.model.database.DatabaseProvisioningListener;

/**
 * Запускает backend для хранения задач и истории действий агентов.
 */
@SpringBootApplication
public class AutoDevAppApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AutoDevAppApplication.class);
        application.addListeners(new DatabaseProvisioningListener());
        application.run(args);
    }
}
