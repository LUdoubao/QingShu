package org.doubao.mall.common.threadpool;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common.threadpool")
public class ThreadPoolProperties {
	/**
	 * 核心线程数（默认：CPU核心数+1）
	 */
	private int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

	/**
	 * 最大线程数（默认：核心线程数*2）
	 */
	private int maxPoolSize = this.corePoolSize * 2;

	/**
	 * 队列容量（默认：100）
	 */
	private int queueCapacity = 100;

	/**
	 * 线程名前缀（默认：common-pool-）
	 */
	private String threadNamePrefix = "common-pool-";

	/**
	 * 线程存活时间（秒，默认：60）
	 */
	private int keepAliveSeconds = 60;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}
}
