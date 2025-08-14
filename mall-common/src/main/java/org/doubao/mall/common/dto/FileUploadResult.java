package org.doubao.mall.common.dto;

public class FileUploadResult {
	private String fileKey;
	private String storageType;
	private String accessUrl;

	public FileUploadResult() {
	}

	public FileUploadResult(String fileKey, String storageType, String fileUrl) {
		this.fileKey = fileKey;
		this.storageType = storageType;
		this.accessUrl = fileUrl;
	}

	public String getFileKey() {
		return fileKey;
	}

	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public String getAccessUrl() {
		return accessUrl;
	}

	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}
}
