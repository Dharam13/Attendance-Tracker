package com.charusat.attendancetracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.util.concurrent.Executors;

/**
 * Centralized scheduling configuration for the application.
 * This class defines the task scheduler and configuration for all scheduling operations.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    @Value("${scheduling.enabled:true}")
    private boolean schedulingEnabled;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (!schedulingEnabled) {
            taskRegistrar.setScheduler(Executors.newScheduledThreadPool(0));
        } else {
            // Use our taskScheduler bean when scheduling is enabled
            taskRegistrar.setScheduler(taskScheduler());
        }
    }

    /**
     * Creates and configures the thread pool task scheduler.
     * This bean will be used by DynamicTaskScheduler.
     */
    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("dynamic-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}