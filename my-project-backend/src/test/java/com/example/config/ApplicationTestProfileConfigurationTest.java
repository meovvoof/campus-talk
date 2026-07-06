package com.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTestProfileConfigurationTest {

    @Test
    void rabbitRetryMultiplierUsesEffectiveExponentialBackoff() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-test.yml"));
        Properties properties = yaml.getObject();

        assertNotNull(properties);
        String multiplier = properties.getProperty("spring.rabbitmq.listener.simple.retry.multiplier");

        assertNotNull(multiplier);
        assertTrue(Double.parseDouble(multiplier) > 1.0);
    }
}
