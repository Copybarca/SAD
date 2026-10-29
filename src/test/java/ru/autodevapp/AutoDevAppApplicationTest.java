package ru.autodevapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class AutoDevAppApplicationTest {

    @Test
    void applicationClassIsSpringBootEntryPoint() {
        assertThat(AutoDevAppApplication.class)
                .hasAnnotation(SpringBootApplication.class);
    }
}
