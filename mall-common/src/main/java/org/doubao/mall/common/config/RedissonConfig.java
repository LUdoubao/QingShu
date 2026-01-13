package org.doubao.mall.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类，用于定义RedissonClient Bean
 */
@Configuration
public class RedissonConfig {

	// 从配置文件读取Redis连接地址（也可直接写死，不推荐）
	@Value("${spring.redis.host:127.0.0.1}")
	private String redisHost;

	@Value("${spring.redis.port:6379}")
	private int redisPort;

	// 如有密码/指定数据库，可添加对应配置项
	@Value("${spring.redis.password:}")
	private String redisPassword;

	@Value("${spring.redis.database:0}")
	private int redisDatabase;

	/**
	 * 定义RedissonClient Bean，供Spring自动注入
	 */
	@Bean(destroyMethod = "shutdown") // 容器销毁时关闭RedissonClient
	public RedissonClient redissonClient() {
		// 创建Redisson配置对象
		Config config = new Config();

		// 配置单节点Redis（集群/哨兵模式可参考Redisson官方文档调整）
		String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);
		config.useSingleServer()
				.setAddress(redisAddress)
				.setPassword(redisPassword.isEmpty() ? null : redisPassword) // 无密码则传null
				.setDatabase(redisDatabase);

		// 创建并返回RedissonClient实例
		return Redisson.create(config);
	}
}