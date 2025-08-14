package org.doubao.oss.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "oss.encryption")
public class KeyConfig {
	// 当前密钥ID
	private String currentKeyId;

	// 密钥映射 (keyId -> base64密钥)
	private Map<String, String> keys = new HashMap<>();

	// 密钥保留期限 (天)
	private int keyRetentionDays = 365;

	// Getters and Setters
	public String getCurrentKeyId() {
		return currentKeyId;
	}

	public void setCurrentKeyId(String currentKeyId) {
		this.currentKeyId = currentKeyId;
	}

	public Map<String, String> getKeys() {
		return keys;
	}

	public void setKeys(Map<String, String> keys) {
		this.keys = keys;
	}

	public int getKeyRetentionDays() {
		return keyRetentionDays;
	}

	public void setKeyRetentionDays(int keyRetentionDays) {
		this.keyRetentionDays = keyRetentionDays;
	}

	// 获取指定密钥
	public String getKey(String keyId) {
		return keys.get(keyId);
	}

	// 获取当前密钥
	public String getCurrentKey() {
		return keys.get(currentKeyId);
	}
}