package org.doubao.oss.service.config;


import org.doubao.oss.service.service.OssService;
import org.doubao.oss.service.service.impl.AliyunOssServiceImpl;
import org.doubao.oss.service.service.impl.LocalStorageServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
	public OssService aliyunOssService() {
		LOGGER.info("Using aliyun OSS storage service.");
		return new AliyunOssServiceImpl(ossProperties);
	}

	@Bean
	@ConditionalOnProperty(name = "oss.storage.type", havingValue = "local", matchIfMissing = true)
	public OssService localStorageService() {
		LOGGER.info("Using local storage service.");
		return new LocalStorageServiceImpl(ossProperties);
	}
}