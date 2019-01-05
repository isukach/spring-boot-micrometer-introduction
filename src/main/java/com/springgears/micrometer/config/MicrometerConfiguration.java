package com.springgears.micrometer.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configs below are NOT necessary when using spring-boot-starter-actuator
 * This is just an example of manual configuration
 */
@Configuration
public class MicrometerConfiguration {

    @Bean
    public MeterRegistry meterRegistry() {
        return new ElasticMeterRegistry(getElasticConfig(), Clock.SYSTEM);
    }

    @Bean
    public ElasticConfig getElasticConfig() {
        return new ElasticConfig() {

            @Override
            public Duration step() {
                return Duration.ofSeconds(30);
            }

            @Override
            public String index() {
                return "spring-gears-metrics";
            }

            @Override
            public String get(String key) {
                return null;
            }
        };
    }
}
