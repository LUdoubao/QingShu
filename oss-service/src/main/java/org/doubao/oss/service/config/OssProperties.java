package org.doubao.oss.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {
	private Aliyun aliyun;
	private Local local;
	private Storage storage;

	public Aliyun getAliyun() {
		return aliyun;
	}

	public void setAliyun(Aliyun aliyun) {
		this.aliyun = aliyun;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public static class Aliyun {
		private String endpoint;
		private String accessKeyId;
		private String accessKeySecret;
		private String bucketName;

		public String getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getAccessKeyId() {
			return accessKeyId;
		}

		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}

		public String getAccessKeySecret() {
			return accessKeySecret;
		}

		public void setAccessKeySecret(String accessKeySecret) {
			this.accessKeySecret = accessKeySecret;
		}

		public String getBucketName() {
			return bucketName;
		}

		public void setBucketName(String bucketName) {
			this.bucketName = bucketName;
		}
	}
	public static class Local {
		private String storagePath;
		private String baseUrl;
		private String defaultAvatar;
		private String aesKey;

		public String getAesKey() {
			return aesKey;
		}

		public void setAesKey(String aesKey) {
			this.aesKey = aesKey;
		}

		public String getDefaultAvatar() {
			return defaultAvatar;
		}

		public void setDefaultAvatar(String defaultAvatar) {
			this.defaultAvatar = defaultAvatar;
		}

		public String getStoragePath() {
			return storagePath;
		}

		public void setStoragePath(String storagePath) {
			this.storagePath = storagePath;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}
	public static class Storage {
		private String type;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

}
