package com.example.e_learning_system;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public Logger testLogger() {
        return (Logger) LoggerFactory.getLogger("TEST");
    }
}
