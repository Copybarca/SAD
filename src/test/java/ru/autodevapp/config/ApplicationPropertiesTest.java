package ru.autodevapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {

    @Test
    void environmentVariablesConfigurePortDatabaseAndPool() throws Exception {
        Properties properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("application.properties"));
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(new MapPropertySource("testEnvironment", Map.of(
                "APP_PORT", "9191",
                "POSTGRES_HOST", "db",
                "POSTGRES_PORT", "5544",
                "POSTGRES_ADMIN_DB", "postgres",
                "POSTGRES_ADMIN_USER", "root",
                "POSTGRES_ADMIN_PASSWORD", "secret",
                "POSTGRES_APP_DB", "agent_history",
                "DB_POOL_SIZE", "7")));
        sources.addLast(new PropertiesPropertySource("application", properties));
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);

        assertThat(resolver.getProperty("server.port")).isEqualTo("9191");
        assertThat(resolver.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db:5544/agent_history");
        assertThat(resolver.getProperty("app.database.admin-url"))
                .isEqualTo("jdbc:postgresql://db:5544/postgres");
        assertThat(resolver.getProperty("spring.datasource.hikari.maximum-pool-size"))
                .isEqualTo("7");
    }
}
