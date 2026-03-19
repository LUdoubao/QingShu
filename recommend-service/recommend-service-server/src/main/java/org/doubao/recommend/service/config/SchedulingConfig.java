package org.doubao.recommend.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务配置类
 * 配置定时任务执行器，支持并发执行多个定时任务
 */
@Configuration
public class SchedulingConfig {

    /**
     * 配置定时任务线程池
     * 用于管理和调度定时任务的执行
     *
     * @return TaskScheduler 任务调度器
     */
    @Bean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 线程池大小，支持多个定时任务并发执行
        scheduler.setThreadNamePrefix("schedule-task-"); // 线程名前缀，便于日志排查
        scheduler.setDaemon(true); // 设置为守护线程
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        scheduler.setAwaitTerminationSeconds(60); // 等待任务完成的最大时间（秒）
        scheduler.initialize();
        return scheduler;
    }
}
