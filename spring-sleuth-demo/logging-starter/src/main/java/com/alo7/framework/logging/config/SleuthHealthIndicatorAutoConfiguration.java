package com.alo7.framework.logging.config;

import com.alo7.framework.logging.health.SleuthHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.sleuth.zipkin2.ZipkinAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Sender;

@Configuration
@AutoConfigureAfter(ZipkinAutoConfiguration.class)
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
public class SleuthHealthIndicatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "sleuthHealthIndicator")
    public SleuthHealthIndicator sleuthHealthIndicator() {
        return new SleuthHealthIndicator();
    }
}
