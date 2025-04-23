package com.charusat.attendancetracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.util.concurrent.Executors;

@Configuration
public class SchedulingConfig {

    @Value("${scheduling.enabled:false}")
    private boolean schedulingEnabled;

    @Bean
    public SchedulingConfigurer schedulingConfigurer() {
        return new SchedulingConfigurer() {
            @Override
            public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
                if (!schedulingEnabled) {
                    taskRegistrar.setScheduler(Executors.newScheduledThreadPool(0));
                }
            }
        };
    }
}