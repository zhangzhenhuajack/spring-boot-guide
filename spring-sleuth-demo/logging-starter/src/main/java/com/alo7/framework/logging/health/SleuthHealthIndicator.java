package com.alo7.framework.logging.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import zipkin2.reporter.Sender;

@Slf4j
public class SleuthHealthIndicator extends AbstractHealthIndicator {
    @Autowired(required = false)
    private Sender sender;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
         if (sender !=null && sender.check().ok()) {
             builder.up().withDetail("messageMaxBytes",sender.messageMaxBytes());
         } else {
             log.warn(" sleuth down");
             builder.down().withDetail("messageMaxBytes",0);
         }
    }

}
