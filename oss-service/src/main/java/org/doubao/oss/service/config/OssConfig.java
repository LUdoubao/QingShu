package org.doubao.oss.service.config;


import org.doubao.oss.service.service.StorageStrategy;
import org.doubao.oss.service.service.impl.AliyunOssStorageStrategy;
import org.doubao.oss.service.service.impl.LocalStorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class OssConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(OssConfig.class);
	@Resource
	private OssProperties ossProperties;

	@Bean
	@ConditionalOnProperty(name = "oss.storage.type", havingValue = "aliyun")
	public StorageStrategy aliyunOssService() {
		LOGGER.info("Using aliyun OSS storage service.");
		return new AliyunOssStorageStrategy(ossProperties);
	}

	@Bean
	@ConditionalOnProperty(name = "oss.storage.type", havingValue = "local", matchIfMissing = true)
	public StorageStrategy localStorageService() {
		LOGGER.info("Using local storage service.");
		return new LocalStorageStrategy(ossProperties);
	}
}