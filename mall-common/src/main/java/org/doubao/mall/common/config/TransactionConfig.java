package org.doubao.mall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

	/**
	 * 配置JDBC事务管理器
	 */
	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 配置事务模板
	 */
	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager manager) {
		TransactionTemplate template = new TransactionTemplate(manager);
		// 设置事务隔离级别（默认：ISOLATION_DEFAULT）
		template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		// 设置传播行为（默认：PROPAGATION_REQUIRED）
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		// 设置超时时间（秒，默认-1表示不超时）
		template.setTimeout(30);
		return template;
	}
}