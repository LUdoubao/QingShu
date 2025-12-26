package org.doubao.fanout.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

	/**
	 * 事件处理线程池
	 */
	@Bean(name = "eventExecutor")
	public Executor eventExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 核心线程数
		executor.setCorePoolSize(10);
		// 最大线程数
		executor.setMaxPoolSize(20);
		// 队列容量
		executor.setQueueCapacity(1000);
		// 线程名称前缀
		executor.setThreadNamePrefix("event-");
		// 线程空闲时间
		executor.setKeepAliveSeconds(60);
		// 拒绝策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	/**
	 * Fanout分发线程池
	 */
	@Bean(name = "fanoutExecutor")
	public Executor fanoutExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(15);
		executor.setMaxPoolSize(30);
		executor.setQueueCapacity(2000);
		executor.setThreadNamePrefix("fanout-");
		executor.setKeepAliveSeconds(60);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	/**
	 * 重试线程池
	 */
	@Bean(name = "retryExecutor")
	public Executor retryExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("retry-");
		executor.setKeepAliveSeconds(60);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
