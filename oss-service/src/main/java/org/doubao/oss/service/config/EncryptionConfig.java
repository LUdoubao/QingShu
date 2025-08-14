package org.doubao.oss.service.config;

import org.doubao.oss.service.service.impl.PathEncryptionService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

	@Bean
	@ConfigurationProperties(prefix = "oss.encryption")
	public KeyConfig keyConfig() {
		return new KeyConfig();
	}

	@Bean
	public PathEncryptionService pathEncryptionService(KeyConfig keyConfig) {
		return new PathEncryptionService(keyConfig);
	}
}