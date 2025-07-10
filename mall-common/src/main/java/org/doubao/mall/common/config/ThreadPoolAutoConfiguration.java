package org.doubao.mall.common.config;

import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.mall.common.threadpool.ThreadPoolProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ThreadPoolTaskExecutor threadPoolTaskExecutor(ThreadPoolProperties properties) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(properties.getCorePoolSize());
		executor.setMaxPoolSize(properties.getMaxPoolSize());
		executor.setQueueCapacity(properties.getQueueCapacity());
		executor.setThreadNamePrefix(properties.getThreadNamePrefix());
		executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	@Bean
	public CommonTaskExecutor commonTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		return new CommonTaskExecutor(threadPoolTaskExecutor);
	}
}