package com.cloud.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhoushuai
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "cloud.web",
        name = {"enableMetrics"},
        matchIfMissing = true
)
@ConditionalOnClass(MeterRegistry.class)
public class MicrometerAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name:app}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }

}
